package com.github.thorqin.toolkit;

import com.github.thorqin.toolkit.annotation.App;
import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.service.IService;
import com.github.thorqin.toolkit.trace.TraceRecorder;
import com.github.thorqin.toolkit.trace.TraceService;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.*;
import com.google.common.base.Strings;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by thor on 9/23/15.
 */
public abstract class Application implements LifeCycleListener, Runnable {
    protected static Map<String, Application> applicationMap = new HashMap<>();
    protected String[] args = null;
    protected Logger logger;
    protected String applicationName;
    protected String configName = "config.json";
    protected AppConfigManager configManager;
    protected String appDataEnv = null;
    protected final Map<String, Class<?>> serviceTypes = new HashMap<>();
    protected final Map<String, Object> serviceMapping = new HashMap<>();
    protected ClassLoader appClassLoader = null;
    protected LogSetting setting = null;
    protected LogHandler logHandler = null;
    protected ConfigManager.ChangeListener configChangeListener = new ConfigManager.ChangeListener() {
        @Override
        public void onConfigChanged(ConfigManager configManager) {
            reloadConfig();
            Application.this.onConfigChanged();
        }
    };

    protected TraceService traceService = new TraceService(new TraceRecorder() {
        @Override
        public void onTrace(Tracer.Info info) {
            if (logger != null)
                logger.log(Level.INFO, info.toString());
        }
    });

    protected static final String NO_APP_NAME_MESSAGE = "Application name is null or empty, use annotation or explicitly call super constructor by pass application name!";
    protected static final String APP_NAME_DUP_MESSAGE = "Application name duplicated!";
    protected static final String INVALID_APP_NAME_MESSAGE = "Application name should be a valid file name and cannot start with '#'";
    protected static final String INVALID_SERVICE_NAME = "Invalid service name!";
    protected static final String SERVICE_NAME_DUPLICATED = "Service name already in used.";
    protected static final String SERVICE_NAME_NOT_REGISTERED = "Service name not registered.";
    protected static final String NEED_APPLICATION_NAME = "Must provide application name when more than one instance exiting.";
    protected static final String UNSUPPORTED_CONFIG_FILE_TYPE = "Unsupported config file type, can only be '*.json' or '*.yml'.";

    private static class LogSetting {
        public boolean async = true;
        public String level = "INFO";
        public String filter = null;
    }

    @Override
    public abstract void run();

    @Override
    public void onStartup() {}

    @Override
    public void onShutdown() {}

    public Application(String name) {
        setName(name);
        init();
        onStartup();
    }

    public Application(String name, String appDataEnv) {
        setName(name);
        this.appDataEnv = appDataEnv;
        init();
        onStartup();
    }

    public Application(String name, String appDataEnv, Service[] services) {
        setName(name);
        this.appDataEnv = appDataEnv;
        setServices(services);
        init();
        onStartup();
    }

    public Application(String name, String appDataEnv, Service[] services, String configName) {
        setName(name);
        this.appDataEnv = appDataEnv;
        setServices(services);
        this.configName = configName;
        init();
        onStartup();
    }

    public void setTraceRecorder(TraceRecorder recorder) {
        traceService.setRecorder(recorder);
    }

    protected Application() {
        App appAnno = this.getClass().getAnnotation(App.class);
        if (appAnno != null) {
            setName(appAnno.name());
            appDataEnv = appAnno.appDataEnv();
            setServices(appAnno.services());
            configName = appAnno.configName();
            init();
        }
    }

    protected void onConfigChanged() {}

    protected synchronized void setServices(Service[] services) {
        for (Service service: services) {
            String serviceName = service.value();
            checkServiceName(serviceName);
            serviceTypes.put(serviceName, service.type());
        }
    }

    protected void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new RuntimeException(NO_APP_NAME_MESSAGE);
        if (!name.matches("[^#/\\\\\\?\\*:\\|\"<>][^/\\\\\\?\\*:\\|\"<>]*"))
            throw new RuntimeException(INVALID_APP_NAME_MESSAGE);
        if (applicationMap.containsKey(name.trim()))
            throw new RuntimeException(APP_NAME_DUP_MESSAGE);
        applicationMap.put(name.trim(), this);
        applicationName = name.trim();
    }

    protected void initLogger() {
        if (logger == null)
            logger = Logger.getLogger(applicationName);
        Handler[] handlers = logger.getHandlers();
        for (Handler handler: handlers) {
            logger.removeHandler(handler);
        }
        setting = configManager.get("/log", LogSetting.class, new LogSetting());
        if (logHandler != null)
            logHandler.close();
        logHandler = new LogHandler(getDataDir("log"), applicationName, setting.async);
        logger.addHandler(logHandler);
        logger.setUseParentHandlers(false);
        Level level;
        try {
            level = Level.parse(setting.level);
        } catch (IllegalArgumentException e) {
            level = Level.INFO;
        }
        logger.setLevel(level);
        if (setting.filter != null && !setting.filter.isEmpty()) {
            try {
                final Pattern pattern = Pattern.compile(setting.filter);
                logger.setFilter(new Filter() {
                    @Override
                    public boolean isLoggable(LogRecord record) {
                        Matcher matcher = pattern.matcher(record.getMessage());
                        if (matcher != null && matcher.find())
                            return false;
                        else
                            return true;
                    }
                });
            } catch (Exception e) {
                System.err.println("Log filter is not a valid regex expression: " + e.getMessage());
            }
        } else {
            logger.setFilter(null);
        }
    }

    public void destroy() {
        for (String name : serviceMapping.keySet()) {
            Object service = serviceMapping.get(name);
            callServiceStop(service, name);
        }
        traceService.stop();
        applicationMap.remove(applicationName);
        logger.log(Level.INFO, "Application stopped! ({0})", applicationName);
        if (logHandler != null)
            logHandler.close();
        if (configManager != null)
            configManager.stopWatch();
        this.onShutdown();
    }

    public static ClassLoader createAppClassLoader(ClassLoader parent, String searchPath) {
        return new AppClassLoader(parent, searchPath);
    }

    /**
     * Get application defined class loader which extends original
     * class loader by add an additional search path.
     * Additional search path is under the application's data path, named 'lib'.
     * @return Application class loader
     */
    public ClassLoader getAppClassLoader() {
        return this.appClassLoader;
    }

    public final String getName() {
        return applicationName;
    }

    public final Logger getLogger() {
        return logger;
    }

    public final Tracer getTracer() {
        return traceService;
    }

    private synchronized void reloadConfig() {
        LogSetting newSetting = configManager.get("/log", LogSetting.class);
        if (!Serializer.equals(setting, newSetting)) {
            initLogger();
        }
        for (String key: serviceTypes.keySet()) {
            Object service;
            if (serviceMapping.containsKey(key)) {
                service = serviceMapping.get(key);
            } else {
                service = createServiceInstance(key);
            }
            callServiceConfig(service, configManager, key, true);
        }
    }

    public String getGroupName() {
        int pos = applicationName.indexOf("#");
        if (pos > 0) {
            return applicationName.substring(0, pos);
        } else
            return applicationName;
    }

    private void scanClasses(File path) throws Exception {
        if (path == null) {
            return;
        }
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File item : files) {
                    scanClasses(item);
                }
            }
            return;
        }
        else if (!path.isFile() || !path.getName().endsWith(".class")) {
            return;
        }
        try (DataInputStream fstream = new DataInputStream(new FileInputStream(path.getPath()))){
            ClassFile cf = new ClassFile(fstream);
            String className = cf.getName();
            AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(
                    AnnotationsAttribute.visibleTag);
            if (visible == null) {
                return;
            }
            for (javassist.bytecode.annotation.Annotation ann : visible.getAnnotations()) {
                if (!ann.getTypeName().equals(Service.class.getName())) {
                    continue;
                }
                // Class<?> clazz = Class.forName(className);
                Class<?> clazz = appClassLoader.loadClass(className);
                if (clazz == null) {
                    continue;
                }
                Service service = clazz.getAnnotation(Service.class);
                if (service == null)
                    continue;
                String appName = service.application().trim();
                if (!appName.isEmpty() && !appName.equals(applicationName)) {
                    continue;
                }
                String serviceName = service.value();
                synchronized (this) {
                    checkServiceName(serviceName);
                    serviceTypes.put(serviceName, clazz);
                }
            }
        }
    }

    protected final void createAllServiceInstance() {
        for (String name : serviceTypes.keySet()) {
            if (!serviceMapping.containsKey(name))
                createServiceInstance(name);
        }
    }

    protected final void startAllService() {
        for (String name : serviceMapping.keySet()) {
            Object service = serviceMapping.get(name);
            callServiceStart(service, name);
        }
    }

    protected final void configAllService() {
        for (String name : serviceMapping.keySet()) {
            Object service = serviceMapping.get(name);
            callServiceConfig(service, configManager, name, false);
        }
    }

    protected final void inject() {
        bindingField(this, getClass());
        for (String key : serviceTypes.keySet()) {
            bindingField(key);
        }
    }

    protected final void init() {
        appClassLoader = createAppClassLoader(Application.class.getClassLoader(), getDataDir("lib"));
        // 1. Init config manager
        if (!configName.matches("^.+\\.(json|yml)$"))
            throw new RuntimeException(UNSUPPORTED_CONFIG_FILE_TYPE);
        try {
            this.configManager = new AppConfigManager(this, configName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 2. Init logger
        initLogger();
        // 3. Start trace service
        traceService.start();
        // 4. Find and inject services
        try {
            File file = new File(Application.class.getResource("/").toURI());
            scanClasses(file);
            File fileExtend = new File(getDataDir("lib"));
            scanClasses(fileExtend);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        createAllServiceInstance();
        inject();
        for (String key: serviceMapping.keySet()) {
            logger.log(Level.INFO, "Register Service: " + key + " -> " + serviceMapping.get(key).getClass().getName());
        }
        configAllService();
        // 5. Start all services which need be started.
        startAllService();
        logger.log(Level.INFO, "Application started! ({0})", applicationName);

        configManager.addChangeListener(configChangeListener);
        configManager.startWatch();
    }

    public static Application get(String appName) {
        if (appName == null)
            return get();
        else
            return applicationMap.get(appName);
    }

    public static Application get() {
        if (applicationMap.size() == 1) {
            Iterator<Map.Entry<String, Application>> iterator = applicationMap.entrySet().iterator();
            if (iterator.hasNext())
                return iterator.next().getValue();
            else
                return null;
        } else {
            throw new RuntimeException(NEED_APPLICATION_NAME);
        }
    }

    public final String getDataDir() {
        return getAppDataDir(appDataEnv, getGroupName());
    }

    public final String getDataDir(String subDir) {
        while (subDir.startsWith("/") || subDir.startsWith("\\"))
            subDir = subDir.substring(1);
        return new File(getDataDir()).toPath().resolve(subDir).toString();
    }

    public final AppConfigManager getConfigManager() {
        return configManager;
    }

    private void bindingField(String key) {
        Class<?> clazz = serviceTypes.get(key);
        if (clazz == null)
            return;
        Object service = serviceMapping.get(key);
        bindingField(service, clazz);
    }

    private void bindingField(Object service, Class<?> clazz) {
        for (Field field : Serializer.getVisibleFields(clazz)) {
            try {
                Service annotation = field.getAnnotation(Service.class);
                if (annotation != null) {
                    field.setAccessible(true);
                    Object fieldValue = getService(annotation.value());
                    if (fieldValue == null) {
                        logger.log(Level.WARNING, "Service not registered: " + annotation.value());
                    }
                    field.set(service, fieldValue);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Service binding failed: <" +
                        clazz.getName() + "::" + field.getName() + ">: " + ex.getMessage());
            }
        }
    }

    public final synchronized void unregisterService(String name) {
        Object service = serviceMapping.remove(name);
        if (service != null)
            callServiceStop(service, name);
        serviceTypes.remove(name);
        logger.log(Level.INFO, "Un-Register Service: " + name);
        inject();
    }

    public final synchronized void registerService(String name, Class<?> type) {
        checkServiceName(name);
        serviceTypes.put(name, type);
        Object service = createServiceInstance(name);
        logger.log(Level.INFO, "Register Service: " + name + " -> " + type.getName());
        inject();
        callServiceStart(service, name);
    }

    private final void checkServiceName(String name) {
        if (Strings.isNullOrEmpty(name))
            throw new RuntimeException(INVALID_SERVICE_NAME);
        if (serviceTypes.containsKey(name)
                || name.matches("config|logger|application|tracer"))
            throw new RuntimeException(SERVICE_NAME_DUPLICATED);
    }

    @SuppressWarnings("unchecked")
    public final synchronized <T> T getService(String name) {
        if (name.equals("config"))
            return (T)configManager;
        if (name.equals("logger"))
            return (T)logger;
        if (name.equals("tracer"))
            return (T)traceService;
        if (name.equals("application"))
            return (T)this;
        if (serviceMapping.containsKey(name)) {
            return (T)serviceMapping.get(name);
        } else {
            return null;
        }
    }

    public final synchronized Set<String> getServiceKeys() {
        return serviceTypes.keySet();
    }

    private Object createServiceInstance(String name) {
        Class<?> type = serviceTypes.get(name);
        if (type == null)
            throw new RuntimeException(SERVICE_NAME_NOT_REGISTERED);
        Object obj;
        try {
            Constructor<?> constructor = type.getConstructor(ConfigManager.class, String.class, Tracer.class);
            constructor.setAccessible(true);
            obj = constructor.newInstance(configManager, name, traceService);
        } catch (NoSuchMethodException ex) {
            try {
                Constructor<?> constructor = type.getConstructor();
                constructor.setAccessible(true);
                obj = constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        serviceMapping.put(name, obj);
        return obj;
    }

    private void callServiceStart(Object serviceInstance, String serviceName) {
        if (IService.class.isInstance(serviceInstance)) {
            IService service = (IService)serviceInstance;
            try {
                service.start();
                logger.log(Level.INFO, "Service start! ({0})", serviceName);
            } catch (Exception ex) {
                LogRecord record = new LogRecord(Level.SEVERE, "Start service error! (" + serviceName + ")");
                record.setThrown(ex);
                logger.log(record);
            }
        }
    }

    private void callServiceStop(Object serviceInstance, String serviceName) {
        if (IService.class.isInstance(serviceInstance)) {
            IService service = (IService)serviceInstance;
            if (service.isStarted()) {
                try {
                    service.stop();
                    logger.log(Level.INFO, "Service stopped! ({0})", serviceName);
                } catch (Exception ex) {
                    LogRecord record = new LogRecord(Level.SEVERE, "Stop service error. (" + serviceName + ")");
                    record.setThrown(ex);
                    logger.log(record);
                }
            }
        } else if (AutoCloseable.class.isInstance(serviceInstance)) {
            AutoCloseable closeable = (AutoCloseable)serviceInstance;
            try {
                closeable.close();
                logger.log(Level.INFO, "Service closed! ({0})", serviceName);
            } catch (Exception ex) {
                LogRecord record = new LogRecord(Level.SEVERE, "Close service error. (" + serviceName + ")");
                record.setThrown(ex);
                logger.log(record);
            }
        }
    }

    private void callServiceConfig(Object serviceInstance, ConfigManager configManager, String serviceName, boolean isReConfig) {
        if (IService.class.isInstance(serviceInstance)) {
            IService service = (IService)serviceInstance;
            try {
                boolean needRestart = service.config(configManager, serviceName, isReConfig);
                if (needRestart && service.isStarted()) {
                    service.stop();
                    service.start();
                    logger.log(Level.INFO, "Service restart! ({0})", serviceName);
                }
            } catch (Exception ex) {
                LogRecord record = new LogRecord(Level.SEVERE, "Service config exception! (" + serviceName + ")");
                record.setThrown(ex);
                logger.log(record);
            }
        }
    }

    /**
     * Get application data directory path, it's combined by Java system property name or
     * OS environment variable name(by default var name is 'APP_DATA_DIR') and plus application name.
     * @param environmentValueName Java system property name or OS environment variable name
     * @param appName Application name, if pass null then return the path of the 'APP_DATA_DIR'
     * @return App data dir path or null if env variable not exists.
     */
    public static String getAppDataDir(String environmentValueName, String appName) {
        if (environmentValueName == null)
            environmentValueName = "app.data.dir";
        String envStyle = environmentValueName.replace('.', '_').toUpperCase();
        String javaStyle = environmentValueName.replace('_', '.').toLowerCase();
        String dataDir = System.getProperty(javaStyle);
        if (dataDir == null) { // there have many nested blocks because for performance reason
            dataDir = System.getProperty(envStyle);
        }
        if (dataDir == null) {
            dataDir = System.getenv(envStyle);
        }
        if (dataDir == null) {
            dataDir = System.getenv(javaStyle);
        }
        if (dataDir == null) {
            String osName = System.getProperty("os.name");
            if (osName.matches("(?i)windows.*")) {
                dataDir = System.getenv("APPDATA");
            } else {
                dataDir = System.getProperty("user.home") + "/.appdata";
            }
        }
        if (dataDir == null) {
            dataDir = System.getProperty("java.io.tmpdir");
        }

        if (dataDir != null) {
            File dir = new File(dataDir);
            if (appName == null) {
                return dir.toString();
            }
            while (appName.startsWith("/") || appName.startsWith("\\"))
                appName = appName.substring(1);
            return dir.toPath().resolve(appName).toString();
        } else
            throw new RuntimeException("Cannot obtain application's data directory!");
    }


    /**
     * Get application data directory path, it's combined by
     * system environment variable 'APP_DATA_DIR' or 'app.data.dir' and plus application name.
     * @param appName Application name
     * @return App data dir path or null if env variable not exists.
     */
    public static String getAppDataDir(String appName) {
        return getAppDataDir(null, appName);
    }

    /**
     * Get application data directory path, it's combined by
     * system environment variable 'APP_DATA_DIR' or 'app.data.dir'.
     * @return App data dir path or null if env variable not exists.
     */
    public static String getAppDataDir() {
        return getAppDataDir(null, null);
    }


    private static class CacheFile {
        public FileMonitor.Monitor monitor;
        public String content;
    }
    final private Map<Path, CacheFile> textFileCache = new HashMap<>();
    final private FileMonitor.FileChangeListener changeListener = new FileMonitor.FileChangeListener() {
        @Override
        public void onFileChange(File file, FileMonitor.ChangeType changeType, Object param) {
            Path key = file.getAbsoluteFile().toPath();
            CacheFile cacheFile;
            synchronized (textFileCache) {
                cacheFile = textFileCache.get(key);
                if (cacheFile == null)
                    return;
            }
            try {
                if (param != null) {
                    cacheFile.content = readAppTextFile(key.toString(), (String) param);
                } else {
                    cacheFile.content = readAppTextFile(key.toString());
                }
            } catch (IOException ex) {
                logger.log(Level.WARNING, "Reload cached file failed!", ex);
            }
        }
    };


    /**
     * Load file from APP_DATA or class loader directory if file not existing in APP_DATA directory.
     * @param fileName file name, may contain sub directory part of the full path.
     * @param encoding file encoding.
     * @return text content.
     * @throws IOException Exception when read file failed.
     */
    public String readAppTextFile(String fileName, String encoding) throws IOException {
        return readAppTextFile(fileName, encoding, false);
    }

    /**
     * Load file from APP_DATA or class loader directory if file not existing in APP_DATA directory.
     * @param fileName file name, may contain sub directory part of the full path.
     * @param encoding file encoding.
     * @param cached Whether or not cache the file content, if file changed cache will be refresh automatically.
     * @return text content.
     * @throws IOException Exception when read file failed.
     */
    public String readAppTextFile(String fileName, String encoding, boolean cached) throws IOException {
        File file = new File(getDataDir() + "/" + fileName);
        Path key = file.getAbsoluteFile().toPath();
        if (cached) {
            synchronized (textFileCache) {
                if (textFileCache.containsKey(key))
                    return textFileCache.get(key).content;
            }
        }
        String content;
        if (file.exists()) {
            content = Serializer.readTextFile(file, encoding);
        } else {
            content = Serializer.readTextResource(fileName, encoding);
        }
        if (cached) {
            synchronized (textFileCache) {
                CacheFile cacheFile = new CacheFile();
                cacheFile.monitor = FileMonitor.watch(key.toString(), changeListener, encoding);
                cacheFile.content = content;
                textFileCache.put(key, cacheFile);
            }
        }
        return content;
    }

    /**
     * Load file from APP_DATA or class loader directory if file not existing in APP_DATA directory.
     * @param fileName file name, may contain sub directory part of the full path.
     * @return text content, text encoding will be auto detected.
     * @throws IOException Exception when read file failed.
     */
    public String readAppTextFile(String fileName) throws IOException {
        return readAppTextFile(fileName, false);
    }

    /**
     * Load file from APP_DATA or class loader directory if file not existing in APP_DATA directory.
     * @param fileName file name, may contain sub directory part of the full path.
     * @param cached Whether or not cache the file content, if file changed cache will be refresh automatically.
     * @return text content, text encoding will be auto detected.
     * @throws IOException Exception when read file failed.
     */
    public String readAppTextFile(String fileName, boolean cached) throws IOException {
        File file = new File(getDataDir() + "/" + fileName);
        Path key = file.getAbsoluteFile().toPath();
        if (cached) {
            synchronized (textFileCache) {
                if (textFileCache.containsKey(key))
                    return textFileCache.get(key).content;
            }
        }
        String content;
        if (file.exists()) {
            content = Serializer.readTextFile(file);
        } else {
            content = Serializer.readTextResource(fileName);
        }
        if (cached) {
            synchronized (textFileCache) {
                CacheFile cacheFile = new CacheFile();
                cacheFile.monitor = FileMonitor.watch(key.toString(), changeListener);
                cacheFile.content = content;
                textFileCache.put(key, cacheFile);
            }
        }
        return content;
    }

    public byte[] readAppFile(String fileName) throws IOException {
        File file = new File(getDataDir() + "/" + fileName);
        if (file.exists()) {
            return Serializer.readFile(file);
        } else {
            return Serializer.readResource(fileName);
        }
    }


    public String readScript(String fileName) throws IOException {
        return readAppTextFile(fileName, true);
    }

    public void removeCache(String fileName) {
        File file = new File(getDataDir() + "/" + fileName);
        Path key = file.getAbsoluteFile().toPath();
        synchronized (textFileCache) {
            CacheFile cacheFile = textFileCache.get(key);
            if (cacheFile != null) {
                cacheFile.monitor.close();
            }
            textFileCache.remove(key);
        }
    }

    public void removeAllCaches() {
        synchronized (textFileCache) {
            for (Path path : textFileCache.keySet()) {
                CacheFile cacheFile = textFileCache.get(path);
                cacheFile.monitor.close();
            }
            textFileCache.clear();
        }
    }

    private static void createApplication(File path, List<Application> appList) throws Exception {
        if (path == null) {
            return;
        }
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File item : files) {
                    createApplication(item, appList);
                }
            }
            return;
        }
        else if (!path.isFile() || !path.getName().endsWith(".class")) {
            return;
        }
        try (DataInputStream fstream = new DataInputStream(new FileInputStream(path.getPath()))){
            ClassFile cf = new ClassFile(fstream);
            String className = cf.getName();
            AnnotationsAttribute visible = (AnnotationsAttribute) cf.getAttribute(
                    AnnotationsAttribute.visibleTag);
            if (visible == null) {
                return;
            }
            for (javassist.bytecode.annotation.Annotation ann : visible.getAnnotations()) {
                if (!ann.getTypeName().equals(App.class.getName())) {
                    continue;
                }
                Class<?> clazz = Class.forName(className);
                if (clazz == null) {
                    continue;
                }
                if (clazz.equals(Application.class))
                    continue;
                if (!Application.class.isAssignableFrom(clazz)) {
                    continue;
                }
                Application app = (Application)clazz.newInstance();
                appList.add(app);
            }
        }
    }

    public static void main(String args[]) throws Exception {
        List<Application> appList = new LinkedList<>();
        File file = new File(Application.class.getResource("/").toURI());
        createApplication(file, appList);
        for (Application app: appList) {
            app.args = args;
            app.onStartup();
        }
        if (appList.size() == 1) {
            Application app = appList.get(0);
            app.run();
            app.destroy();
        } else {
            for (final Application app: appList) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        app.run();
                        app.destroy();
                    }
                });
                thread.setDaemon(false);
                thread.start();
            }
        }
    }
}
