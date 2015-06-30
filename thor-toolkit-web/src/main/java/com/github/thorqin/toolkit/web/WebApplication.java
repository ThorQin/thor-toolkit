/*
 * The MIT License
 *
 * Copyright 2014 nuo.qin.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.service.ISettingComparable;
import com.github.thorqin.toolkit.service.IStartable;
import com.github.thorqin.toolkit.service.IStoppable;
import com.github.thorqin.toolkit.trace.TraceService;
import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.AppClassLoader;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.filter.WebFilterBase;
import com.github.thorqin.toolkit.web.filter.WebSecurityManager;
import com.github.thorqin.toolkit.web.router.WebBasicRouter;
import com.github.thorqin.toolkit.web.router.WebRouterBase;
import com.github.thorqin.toolkit.web.session.ClientSession;
import com.github.thorqin.toolkit.web.session.WebSession;
import com.github.thorqin.toolkit.web.utility.UploadManager;
import com.google.common.base.Strings;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.*;
import javax.servlet.*;

/**
 *
 * @author nuo.qin
 */
public abstract class WebApplication extends TraceService
		implements ServletContextListener, ConfigManager.ChangeListener, LifeCycleListener {

	private static final String NO_APP_NAME_MESSAGE = "Web application name is null or empty, use @WebApp annotation or explicitly call super constructor by pass application name!";
	private static final String APP_NAME_DUP_MESSAGE = "Web application name duplicated!";
    private static final String INVALID_SERVICE_NAME = "Invalid service name!";
    private static final String SERVICE_NAME_DUPLICATED = "Service name already in used.";
    private static final String SERVICE_NAME_NOT_REGISTERED = "Service name not registered.";
    private static final String NEED_APPLICATION_NAME = "Must provide application name when more than one instance exiting.";

	public static class Setting {
        public boolean compressJs = true;
		public boolean traceRouter = false;
		public boolean traceAccess = false;
		public int sessionTimeout = 900;
	}

	public static class RouterInfo {
		public String[] path;
		public WebRouterBase router;
		public RouterInfo(String[] path, WebRouterBase router) {
			this.path = path;
			this.router = router;
		}
	}

	public static class FilterInfo {
		public String[] path;
		public WebFilterBase filter;
		public FilterInfo(String[] path, WebFilterBase filter) {
			this.path = path;
			this.filter = filter;
		}
	}


	private static Map<String, WebApplication> applicationMap = new HashMap<>();

    private final Logger logger;
	private String applicationName;
	private ConfigManager configManager;
	private ServletContext servletContext;
	private List<RouterInfo> routers = null;
	private List<FilterInfo> filters = null;
	private Class<? extends WebSession> sessionType = ClientSession.class;
    private final Map<String, Class<?>> serviceTypes = new HashMap<>();
    private final Map<String, Object> serviceMapping = new HashMap<>();
	private Setting setting;
    private String appDataEnv = null;

	public WebApplication(String name) {
		if (name == null || name.isEmpty())
			throw new RuntimeException(NO_APP_NAME_MESSAGE);
		if (applicationMap.containsKey(name))
			throw new RuntimeException(APP_NAME_DUP_MESSAGE);
		applicationMap.put(name, this);
		applicationName = name;
        logger = Logger.getLogger(applicationName);
		init();
	}

	public WebApplication() {
		String name = null;
		WebApp appAnno = this.getClass().getAnnotation(WebApp.class);
		if (appAnno != null)
			name = appAnno.name();
		if (name == null || name.isEmpty())
			throw new RuntimeException(NO_APP_NAME_MESSAGE);
		if (applicationMap.containsKey(name))
			throw new RuntimeException(APP_NAME_DUP_MESSAGE);
		applicationMap.put(name, this);
		applicationName = name;
		sessionType = appAnno.sessionType();
        logger = Logger.getLogger(applicationName);
        for (Service service: appAnno.services()) {
            String serviceName = service.value();
            if (Strings.isNullOrEmpty(serviceName)) {
                throw new RuntimeException(INVALID_SERVICE_NAME);
            }
            serviceTypes.put(serviceName, service.type());
        }
        try {
            File file = new File(WebBasicRouter.class.getResource("/").toURI());
            scanClasses(file, serviceTypes, applicationName);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
		init();
	}

    private static void scanClasses(File path, Map<String, Class<?>> serviceTypes, String applicationName) throws Exception {
        if (path == null) {
            return;
        }
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File item : files) {
                    scanClasses(item, serviceTypes, applicationName);
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
                Class<?> clazz = Class.forName(className);
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

    public synchronized void unregisterService(String name) {
        serviceTypes.remove(name);
        serviceMapping.remove(name);
    }

    public synchronized void registerService(String name, Class<?> type) {
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

	public final Class<? extends WebSession> getSessionType() {
		return sessionType;
	}

	public static WebApplication get(String appName) {
        if (appName == null)
            return get();
        else
		    return applicationMap.get(appName);
	}

	public static WebApplication get() {
		if (applicationMap.size() == 1) {
			Iterator<Map.Entry<String, WebApplication>> iterator = applicationMap.entrySet().iterator();
			if (iterator.hasNext())
				return iterator.next().getValue();
			else
				return null;
		} else {
			throw new RuntimeException(NEED_APPLICATION_NAME);
		}
	}

    public AppClassLoader getAppClassLoader(ClassLoader parent) {
        String path;
        try {
            path = getDataPath("lib");
        } catch (Exception ex) {
            path = null;
        }
        AppClassLoader appClassLoader = new AppClassLoader(parent, path);
        return appClassLoader;
    }

	@Override
	public void onConfigChanged(ConfigManager configManager) {
		loadConfig();
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

	private synchronized void loadConfig() {
		setting = configManager.get("/", Setting.class, new Setting());
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

    public Logger getLogger() {
        return logger;
    }

	private void init() {
        WebApp appAnno = this.getClass().getAnnotation(WebApp.class);
        if (appAnno != null)
            appDataEnv = appAnno.appDataEnv();
        final String dataDir = ConfigManager.getAppDataDir(appDataEnv, applicationName);
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
		this.configManager = new ConfigManager();
        this.configManager.setAppName(applicationName);
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

	/**
	 * If 
	 * @param servletContext
	 * @throws ServletException 
	 */
	final void onStartup(ServletContext servletContext) throws ServletException {
		this.servletContext = servletContext;
		this.onStartup();
	}

	@Override
	protected void onTraceInfo(Info info) {
		if (logger != null)
			logger.log(Level.INFO, info.toString());
	}
	
	@Override
	public final void contextInitialized(ServletContextEvent sce) {}

	@Override
	public final synchronized void contextDestroyed(ServletContextEvent sce) {
        for (String name : serviceMapping.keySet()) {
            Object service = serviceMapping.get(name);
            callServiceStop(service);
        }
		try {
            this.onShutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
		this.stop();
		applicationMap.remove(applicationName);
	}

	public final ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public void onStartup() throws ServletException {}

	@Override
	public void onShutdown() {}

	public final String getName() {
		return applicationName;
	}

	public final String getDataPath() throws MalformedURLException, URISyntaxException {
		String dataDir = ConfigManager.getAppDataDir(appDataEnv, applicationName);
		if (dataDir != null) {
			return dataDir;
		} else {
			File dataPath = new File(servletContext.getResource("/WEB-INF/").toURI());
			dataPath = new File(dataPath.getAbsolutePath() + "/data");
			return dataPath.getAbsolutePath();
		}
	}
	
	public final String getDataPath(String subDir) throws MalformedURLException, URISyntaxException {
		String baseDir = getDataPath();
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

	public final UploadManager getUploadManager(String uploadFolderName, boolean storeDetail) {
		try {
			File filePath = new File(getDataPath(uploadFolderName));
			return new UploadManager(filePath, storeDetail);
		} catch (MalformedURLException | URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
	}

	public final UploadManager getUploadManager(String uploadFolderName) {
		return getUploadManager(uploadFolderName, true);
	}

	public final UploadManager getUploadManager() {
		return getUploadManager("upload", true);

	}

	public final ConfigManager getConfigManager() {
		return configManager;
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

    public static <T> T createInstance(Class<T> clazz, WebApplication application) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<? extends T> constructor;
        try {
            constructor = clazz.getConstructor(WebApplication.class);
            constructor.setAccessible(true);
            return constructor.newInstance(application);
        } catch (NoSuchMethodException ex) {
            constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
    }

	public final List<RouterInfo> getRouters() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		if (routers == null) {
			routers = new LinkedList<>();
			WebApp webApp = this.getClass().getAnnotation(WebApp.class);
			if (webApp == null) {
                WebRouterBase inst = new WebBasicRouter(this);
                configManager.addChangeListener(inst);
				routers.add(new RouterInfo(new String[]{"/*"}, inst));
			} else {
				for (WebRouter router: webApp.routers()) {
					try {
						WebRouterBase inst = createInstance(router.type(), this);
						configManager.addChangeListener(inst);
						routers.add(new RouterInfo(router.value(), inst));
					} catch (Exception ex) {
						logger.log(Level.SEVERE, "Create router failed!", ex);
					}
				}
			}
		}
		return routers;
	}

	public final List<FilterInfo> getFilters() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		if (filters == null) {
			filters = new LinkedList<>();
			WebApp webApp = this.getClass().getAnnotation(WebApp.class);
			if (webApp == null) {
                WebFilterBase inst = new WebSecurityManager(this);
                configManager.addChangeListener(inst);
				filters.add(new FilterInfo(new String[]{"/*"}, inst));
			} else {
				for (WebFilter filter: webApp.filters()) {
					try {
						WebFilterBase inst = createInstance(filter.type(), this);
						configManager.addChangeListener(inst);
						filters.add(new FilterInfo(filter.value(), inst));
					} catch (Exception ex) {
						logger.log(Level.SEVERE, "Create filter failed!", ex);
					}
				}
			}
		}
		return filters;
	}

	public final Setting getSetting() {
		return setting;
	}
}
