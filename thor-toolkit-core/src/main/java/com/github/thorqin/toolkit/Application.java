package com.github.thorqin.toolkit;

import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.service.ISettingComparable;
import com.github.thorqin.toolkit.service.IStartable;
import com.github.thorqin.toolkit.service.IStoppable;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by thor on 9/23/15.
 */
public class Application extends TraceService implements ConfigManager.ChangeListener {
    protected static Map<String, Application> applicationMap = new HashMap<>();
    protected Logger logger;
    protected String applicationName;
    protected AppConfigManager configManager;
    protected String appDataEnv = null;
    protected final Map<String, Class<?>> serviceTypes = new HashMap<>();
    protected final Map<String, Object> serviceMapping = new HashMap<>();
    protected ClassLoader appClassLoader = null;

    protected static final String NO_APP_NAME_MESSAGE = "Application name is null or empty, use annotation or explicitly call super constructor by pass application name!";
    protected static final String APP_NAME_DUP_MESSAGE = "Application name duplicated!";
    protected static final String INVALID_APP_NAME_MESSAGE = "Application name should be a valid file name and cannot start with '#'";
    protected static final String INVALID_SERVICE_NAME = "Invalid service name!";
    protected static final String SERVICE_NAME_DUPLICATED = "Service name already in used.";
    protected static final String SERVICE_NAME_NOT_REGISTERED = "Service name not registered.";
    protected static final String NEED_APPLICATION_NAME = "Must provide application name when more than one instance exiting.";

    public Application(String name) {
        setName(name);
        init();
    }

    public Application(String name, String appDataEnv) {
        setName(name);
        this.appDataEnv = appDataEnv;
        init();
    }

    public Application(String name, String appDataEnv, Service[] services) {
        setName(name);
        this.appDataEnv = appDataEnv;
        setServices(services);
        init();
    }

    protected Application() {
    }

    protected void setServices(Service[] services) {
        for (Service service: services) {
            String serviceName = service.value();
            if (Strings.isNullOrEmpty(serviceName)) {
                throw new RuntimeException(INVALID_SERVICE_NAME);
            }
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
        logger = Logger.getLogger(applicationName);
    }

    public void destory() {
        for (String name : serviceMapping.keySet()) {
            Object service = serviceMapping.get(name);
            callServiceStop(service);
        }
        this.stop();
        applicationMap.remove(applicationName);
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

    @Override
    public void onConfigChanged(ConfigManager configManager) {
        reloadConfig();
    }

    private synchronized void reloadConfig() {
        List<Object> restartService = new LinkedList<>();
        for (String key: serviceTypes.keySet()) {
            if (serviceMapping.containsKey(key)) {
                Object service = serviceMapping.get(key);
                if (isServiceSettingChanged(service, configManager, key)) {
                    callServiceStop(service);
                    restartService.add(createServiceInstance(key));
                }
            } else
                restartService.add(createServiceInstance(key));
        }
        inject();
        for (Object obj : restartService) {
            callServiceStart(obj);
        }
    }

    public String getGroupName() {
        int pos = applicationName.indexOf("#");
        if (pos > 0) {
            return applicationName.substring(0, pos);
        } else
            return applicationName;
    }

    private static void scanClasses(File path, Map<String, Class<?>> serviceTypes, String applicationName, ClassLoader classLoader) throws Exception {
        if (path == null) {
            return;
        }
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File item : files) {
                    scanClasses(item, serviceTypes, applicationName, classLoader);
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
                //Class<?> clazz = Class.forName(className);
                Class<?> clazz = classLoader.loadClass(className);
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
                if (Strings.isNullOrEmpty(serviceName)) {
                    throw new RuntimeException(INVALID_SERVICE_NAME);
                }
                serviceTypes.put(serviceName, clazz);
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
            callServiceStart(service);
        }
    }

    protected final void inject() {
        for (String key : serviceTypes.keySet()) {
            bindingField(key);
        }
    }

    protected final void init() {
        final String dataDir = getDataDir();
        appClassLoader = createAppClassLoader(Application.class.getClassLoader(), getDataDir("lib"));
        if (dataDir != null) {
            try {
                String logFile = getDataDir("log");
                Files.createDirectories(new File(logFile).toPath());
                logFile += "/ " + applicationName + ".log";
                try {
                    FileHandler fileHandler = new FileHandler(logFile, 1024 * 1024, 3, true);
                    fileHandler.setEncoding("utf-8");
                    fileHandler.setFormatter(new SimpleFormatter());
                    logger.addHandler(fileHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        this.start(); // Start trace service
        try {
            this.configManager = new AppConfigManager(this, "config.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        configManager.addChangeListener(this);
        configManager.startWatch();

        try {
            File file = new File(Application.class.getResource("/").toURI());
            scanClasses(file, serviceTypes, applicationName, appClassLoader);
            File fileExtend = new File(getDataDir("lib"));
            scanClasses(fileExtend, serviceTypes, applicationName, appClassLoader);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        createAllServiceInstance();
        inject();
        for (String key: serviceMapping.keySet()) {
            System.out.println("Register Service: " + key + " -> " + serviceMapping.get(key).getClass().getName());
        }
        startAllService();
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
        for (Field field : clazz.getDeclaredFields()) {
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
            callServiceStop(service);
        serviceTypes.remove(name);
        inject();
    }

    public final synchronized void registerService(String name, Class<?> type) {
        if (Strings.isNullOrEmpty(name)) {
            throw new RuntimeException(INVALID_SERVICE_NAME);
        }
        if (serviceTypes.containsKey(name) || name.equals("config") || name.equals("logger") || name.equals("application"))
            throw new RuntimeException(SERVICE_NAME_DUPLICATED);
        serviceTypes.put(name, type);
        Object service = createServiceInstance(name);
        System.out.println("Register Service: " + name + " -> " + type.getName());
        inject();
        callServiceStart(service);
    }

    @SuppressWarnings("unchecked")
    public final synchronized <T> T getService(String name) {
        if (name.equals("config"))
            return (T)configManager;
        if (name.equals("logger"))
            return (T)logger;
        if (name.equals("application"))
            return (T)this;
        if (serviceMapping.containsKey(name)) {
            return (T)serviceMapping.get(name);
        } else {
            logger.log(Level.WARNING, "Service not registered: " + name);
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
            obj = constructor.newInstance(configManager, name, this);
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

    private void callServiceStart(Object serviceInstance) {
        if (IStartable.class.isAssignableFrom(serviceInstance.getClass())) {
            IStartable service = (IStartable)serviceInstance;
            service.start();
        }
    }

    private void callServiceStop(Object serviceInstance) {
        if (IStoppable.class.isAssignableFrom(serviceInstance.getClass())) {
            IStoppable service = (IStoppable)serviceInstance;
            service.stop();
        } else if (AutoCloseable.class.isAssignableFrom(serviceInstance.getClass())) {
            AutoCloseable closeable = (AutoCloseable)serviceInstance;
            try {
                closeable.close();
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Close service exception.", ex);
            }
        }
    }

    private boolean isServiceSettingChanged(Object serviceInstance, ConfigManager configManager, String configName) {
        if (ISettingComparable.class.isAssignableFrom(serviceInstance.getClass())) {
            ISettingComparable service = (ISettingComparable)serviceInstance;
            return service.isSettingChanged(configManager, configName);
        } else
            return true;
    }


    @Override
    protected void onTraceInfo(Info info) {
        if (logger != null)
            logger.log(Level.INFO, info.toString());
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

}
