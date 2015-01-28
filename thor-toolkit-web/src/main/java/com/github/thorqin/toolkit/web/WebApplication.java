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
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.web.annotation.WebApp;
import com.github.thorqin.toolkit.web.annotation.WebFilter;
import com.github.thorqin.toolkit.web.annotation.WebRouter;
import com.github.thorqin.toolkit.web.session.ClientSession;
import com.github.thorqin.toolkit.web.session.WebSession;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
		public boolean traceSecurity = false;
		public int maxInterval = 900;
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

	private static Logger logger =
			Logger.getLogger(WebApplication.class.getName());
	private static Map<String, WebApplication> applicationMap = new HashMap<>();

	private String applicationName;
	private ConfigManager configManager;
	private ServletContext servletContext;
	private List<RouterInfo> routers = null;
	private List<FilterInfo> filters = null;
	private Class<? extends WebSession> sessionType = ClientSession.class;
	private final Map<String, DBService> dbMapping = new HashMap<>();
	private Setting setting;

	public WebApplication(String name) {
		if (name == null || name.isEmpty())
			throw new RuntimeException(NO_APP_NAME_MESSAGE);
		if (applicationMap.containsKey(name))
			throw new RuntimeException(APP_NAME_DUP_MESSAGE);
		applicationMap.put(name, this);
		applicationName = name;
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
		init();
	}

	public Class<? extends WebSession> getSessionType() {
		return sessionType;
	}

	public static WebApplication get(String name) {
		return applicationMap.get(name);
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

	@Override
	public void onChanged(ConfigManager config) {
		loadConfig();
	}

	private void loadConfig() {
		setting = configManager.get("/", Setting.class, new Setting());
	}

	private void init() {
		this.start(); // Start trace service
		this.configManager = new ConfigManager();
		configManager.addChangeListener(this);
		try {
			configManager.load(ConfigManager.getAppDataDir(applicationName), "config.json");
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
	protected void onTraceInfo(Info info) {}
	
	@Override
	public final void contextInitialized(ServletContextEvent sce) {}

	@Override
	public final void contextDestroyed(ServletContextEvent sce) {
		try {
			for (Map.Entry<String, DBService> db: dbMapping.entrySet()) {
				db.getValue().close();
			}
		} catch (Exception ex) {
		}
		this.onShutdown();
		this.stop();
		applicationMap.remove(applicationName);
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public void onStartup() {}

	@Override
	public void onShutdown() {}


	public final String getDataPath() throws MalformedURLException, URISyntaxException {
		String dataDir = ConfigManager.getAppDataDir(applicationName);
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
				db.addTracer(this);
			dbMapping.put(name, db);
			return db;
		}
	}
	public final DBService getDBService() throws ValidateException {
		return getDBService("db");
	}

	public final List<RouterInfo> getRouters() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		if (routers == null) {
			routers = new LinkedList<>();
			WebApp webApp = this.getClass().getAnnotation(WebApp.class);
			if (webApp == null) {
				routers.add(new RouterInfo(new String[]{"/*"}, new WebBasicRouter(this)));
			} else {
				for (WebRouter router: webApp.routers()) {
					Constructor<? extends WebRouterBase> constructor =
							router.type().getConstructor(WebApplication.class);
					if (constructor != null) {
						constructor.setAccessible(true);
						WebRouterBase inst = constructor.newInstance(this);
						routers.add(new RouterInfo(router.path(), inst));
					} else {
						constructor = router.type().getConstructor();
						if (constructor != null) {
							constructor.setAccessible(true);
							WebRouterBase inst = constructor.newInstance();
							routers.add(new RouterInfo(router.path(), inst));
						} else {
							throw new NoSuchMethodException("Router must provide a valid constructor see WebRouterBase definition.");
						}
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
				filters.add(new FilterInfo(new String[]{"/*"}, new WebSecurityManager(this)));
			} else {
				for (WebFilter filter: webApp.filters()) {
					Constructor<? extends WebFilterBase> constructor =
							filter.type().getConstructor(WebApplication.class);
					if (constructor != null) {
						constructor.setAccessible(true);
						WebFilterBase inst = constructor.newInstance(this);
						filters.add(new FilterInfo(filter.path(), inst));
					} else {
						constructor = filter.type().getConstructor();
						if (constructor != null) {
							constructor.setAccessible(true);
							WebFilterBase inst = constructor.newInstance();
							filters.add(new FilterInfo(filter.path(), inst));
						} else {
							throw new NoSuchMethodException("Filter must provide a valid constructor see WebFilterBase definition.");
						}
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
