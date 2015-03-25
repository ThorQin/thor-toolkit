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

import com.github.thorqin.toolkit.db.DBService;
import com.github.thorqin.toolkit.trace.TraceService;
import com.github.thorqin.toolkit.utility.AppClassLoader;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.web.annotation.WebApp;
import com.github.thorqin.toolkit.web.annotation.WebFilter;
import com.github.thorqin.toolkit.web.annotation.WebRouter;
import com.github.thorqin.toolkit.web.filter.WebFilterBase;
import com.github.thorqin.toolkit.web.filter.WebSecurityManager;
import com.github.thorqin.toolkit.web.router.WebBasicRouter;
import com.github.thorqin.toolkit.web.router.WebRouterBase;
import com.github.thorqin.toolkit.web.session.ClientSession;
import com.github.thorqin.toolkit.web.session.WebSession;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
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

	public static class Setting {
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
	private final Map<String, DBService> dbMapping = new HashMap<>();
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
		init();
	}

	public final Class<? extends WebSession> getSessionType() {
		return sessionType;
	}

	public static WebApplication get(String appName) {
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
			throw new RuntimeException(
					"Must provide application name when more than one instance exiting.");
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

	private synchronized void loadConfig() {
		setting = configManager.get("/", Setting.class, new Setting());
		for (String dbKey: dbMapping.keySet()) {
			DBService dbService = dbMapping.get(dbKey);
			if (dbService == null)
				continue;
			boolean useTrace = configManager.getBoolean(dbKey + "/trace", false);
			if (useTrace)
				dbService.setTracer(this);
			else
				dbService.setTracer(null);
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
		configManager.addChangeListener(this);
		try {
			configManager.load(dataDir, "config.json");
			configManager.setMonitorFileChange(true);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Configuration file not exists!");
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
		try {
			for (Map.Entry<String, DBService> db: dbMapping.entrySet()) {
				db.getValue().close();
			}
		} catch (Exception ex) {
            ex.printStackTrace();
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

	public final ConfigManager getConfigManager() {
		return configManager;
	}

	public final synchronized DBService getDBService(String name) throws ValidateException {
		if (dbMapping.containsKey(name))
			return dbMapping.get(name);
		else {
			DBService.DBSetting setting = configManager.get(name, DBService.DBSetting.class);
			DBService db = new DBService(setting);
			if (setting.trace)
				db.setTracer(this);
			dbMapping.put(name, db);
			return db;
		}
	}
	public final DBService getDBService() throws ValidateException {
		return getDBService("db");
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
                    WebRouterBase inst = createInstance(router.type(), this);
                    configManager.addChangeListener(inst);
                    routers.add(new RouterInfo(router.value(), inst));
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
                WebFilterBase inst =new WebSecurityManager(this);
                configManager.addChangeListener(inst);
				filters.add(new FilterInfo(new String[]{"/*"}, inst));
			} else {
				for (WebFilter filter: webApp.filters()) {
                    WebFilterBase inst = createInstance(filter.type(), this);
                    configManager.addChangeListener(inst);
                    filters.add(new FilterInfo(filter.value(), inst));
				}
			}
		}
		return filters;
	}

	public final Setting getSetting() {
		return setting;
	}
}
