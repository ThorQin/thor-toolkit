package com.github.thorqin.toolkit;

import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.service.ISettingComparable;
import com.github.thorqin.toolkit.service.IStartable;
import com.github.thorqin.toolkit.service.IStoppable;
import com.github.thorqin.toolkit.trace.TraceService;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.AppClassLoader;
import com.github.thorqin.toolkit.utility.AppConfigManager;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.google.common.base.Strings;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by thor on 9/23/15.
 */
public abstract class Application extends TraceService implements ConfigManager.ChangeListener {
    protected static Map<String, Application> applicationMap = new HashMap<>();
    protected Logger logger;
    protected String applicationName;
    protected AppConfigManager configManager;
    protected String appDataEnv = null;
    protected final Map<String, Class<?>> serviceTypes = new HashMap<>();
    protected final Map<String, Object> serviceMapping = new HashMap<>();

    protected static final String NO_APP_NAME_MESSAGE = "Web application name is null or empty, use @WebApp annotation or explicitly call super constructor by pass application name!";
    protected static final String APP_NAME_DUP_MESSAGE = "Web application name duplicated!";
    protected static final String INVALID_SERVICE_NAME = "Invalid service name!";
    protected static final String SERVICE_NAME_DUPLICATED = "Service name already in used.";
    protected static final String SERVICE_NAME_NOT_REGISTERED = "Service name not registered.";
    protected static final String NEED_APPLICATION_NAME = "Must provide application name when more than one instance exiting.";

    public Application(String name) {
        if (name == null || name.isEmpty())
            throw new RuntimeException(NO_APP_NAME_MESSAGE);
        if (applicationMap.containsKey(name))
            throw new RuntimeException(APP_NAME_DUP_MESSAGE);
        applicationMap.put(name, this);
        applicationName = name;
        logger = Logger.getLogger(applicationName);
        init();
    }

    public Application(String name, String appDataEnv) {
        if (name == null || name.isEmpty())
            throw new RuntimeException(NO_APP_NAME_MESSAGE);
        if (applicationMap.containsKey(name))
            throw new RuntimeException(APP_NAME_DUP_MESSAGE);
        applicationMap.put(name, this);
        applicationName = name;
        logger = Logger.getLogger(applicationName);
        this.appDataEnv = appDataEnv;
        init();
    }

    public Application(String name, String appDataEnv, Service[] services) {
        if (name == null || name.isEmpty())
            throw new RuntimeException(NO_APP_NAME_MESSAGE);
        if (applicationMap.containsKey(name))
            throw new RuntimeException(APP_NAME_DUP_MESSAGE);
        applicationMap.put(name, this);
        applicationName = name;
        logger = Logger.getLogger(applicationName);
        this.appDataEnv = appDataEnv;
        for (Service service: services) {
            String serviceName = service.value();
            if (Strings.isNullOrEmpty(serviceName)) {
                throw new RuntimeException(INVALID_SERVICE_NAME);
            }
            serviceTypes.put(serviceName, service.type());
        }
        init();
    }

    protected Application() {
    }

    public void destory() {
        for (String name : serviceMapping.keySet()) {
            Object service = serviceMapping.get(name);
            callServiceStop(service);
        }
        this.stop();
        applicationMap.remove(applicationName);
    }

    public AppClassLoader getAppClassLoader(ClassLoader parent) {
        String path;
        try {
            path = getDataDir("lib");
        } catch (Exception ex) {
            path = null;
        }
        AppClassLoader appClassLoader = new AppClassLoader(parent, path);
        return appClassLoader;
    }

    public final String getName() {
        return applicationName;
    }

    public final Logger getLogger() {
        return logger;
    }

    @Override
    public void onConfigChanged(ConfigManager configManager) {
        loadConfig();
    }

    private synchronized void loadConfig() {
        for (String key: serviceTypes.keySet()) {
            if (serviceMapping.containsKey(key)) {
                Object service = serviceMapping.get(key);
                if (isServiceSettingChanged(service, configManager, key)) {
                    callServiceStop(service);
                    createServiceInstance(key);
                }
            } else
                createServiceInstance(key);
        }
        for (String key : serviceTypes.keySet()) {
            bindingField(key);
        }
    }

    protected final void init() {
        final String dataDir = getAppDataDir(appDataEnv, applicationName);
        if (dataDir != null) {
            try {
                String logFile = dataDir;
                if (logFile.endsWith("/") || logFile.endsWith("\\"))
                    logFile += "log";
                else
                    logFile += "/log";
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
        try {
            configManager.load(dataDir, "config.json");
            configManager.setMonitorFileChange(true);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Configuration file not exists!");
        }
        for (String key: serviceTypes.keySet()) {
            System.out.println("Register Service: " + key + " -> " + serviceTypes.get(key).getName());
        }
        loadConfig();
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
        return getAppDataDir(appDataEnv, applicationName);
    }

    public final String getDataDir(String subDir) {
        String baseDir = getDataDir();
        if (subDir.startsWith("/") || subDir.startsWith("\\")) {
            if (baseDir.endsWith("/") || baseDir.endsWith("\\"))
                return baseDir + subDir.substring(1);
            else
                return baseDir + subDir;
        } else {
            if (baseDir.endsWith("/") || baseDir.endsWith("\\"))
                return baseDir + subDir;
            else
                return baseDir + "/" + subDir;
        }
    }

    public final AppConfigManager getConfigManager() {
        return configManager;
    }

    private void bindingField(String key) {
        Class<?> clazz = serviceTypes.get(key);
        Object service = serviceMapping.get(key);
        for (Field field : clazz.getDeclaredFields()) {
            try {
                Service annotation = field.getAnnotation(Service.class);
                if (annotation != null) {
                    field.setAccessible(true);
                    Object fieldValue = serviceMapping.get(annotation.value());
                    if (fieldValue == null) {
                        throw new RuntimeException("Service not registered: " + annotation.value());
                    }
                    field.set(service, fieldValue);
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Service binding field failed: " + clazz.getName() + ": " + field.getName(), ex);
            }
        }
    }

    public final synchronized void unregisterService(String name) {
        serviceTypes.remove(name);
        serviceMapping.remove(name);
    }

    public final synchronized void registerService(String name, Class<?> type) {
        if (Strings.isNullOrEmpty(name)) {
            throw new RuntimeException(SERVICE_NAME_DUPLICATED);
        }
        if (serviceTypes.containsKey(name))
            throw new RuntimeException(INVALID_SERVICE_NAME);
        serviceTypes.put(name, type);
        System.out.println("Register Service: " + name + " -> " + type.getName());
        createServiceInstance(name);
        bindingField(name);
    }

    @SuppressWarnings("unchecked")
    public final synchronized <T> T getService(String name) {
        if (serviceMapping.containsKey(name)) {
            return (T)serviceMapping.get(name);
        } else {
            logger.log(Level.WARNING, "Service not registered: " + name);
            return null;
        }
    }

    private void createServiceInstance(String name) {
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
        callServiceStart(obj);
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

        if (dataDir != null) {
            if (appName == null) {
                return dataDir;
            }
            while (appName.startsWith("/") || appName.startsWith("\\"))
                appName = appName.substring(1);
            if (dataDir.endsWith("/") || dataDir.endsWith("\\"))
                dataDir += appName;
            else
                dataDir += ("/" + appName);
            return dataDir;
        } else
            return null;
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
}
