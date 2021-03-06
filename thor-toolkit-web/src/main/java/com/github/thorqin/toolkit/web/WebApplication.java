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

import com.github.thorqin.toolkit.Application;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.web.annotation.*;
import com.github.thorqin.toolkit.web.filter.WebFilterBase;
import com.github.thorqin.toolkit.web.filter.WebSecurityManager;
import com.github.thorqin.toolkit.web.router.WebBasicRouter;
import com.github.thorqin.toolkit.web.router.WebRouterBase;
import com.github.thorqin.toolkit.web.session.ClientSession;
import com.github.thorqin.toolkit.web.session.WebSession;
import com.github.thorqin.toolkit.web.utility.UploadManager;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

/**
 * @author nuo.qin
 */
public abstract class WebApplication extends Application
        implements ServletContextListener {


    public static class Setting {
        public boolean gzip = true;
        public boolean compressJs = true;
        public boolean traceRouter = false;
        public boolean traceAccess = false;

        /**
         * Used only when session is client session, client session use cookie to store session information.
         * Different application should use different 'sessionName' to avoid key conflict.
         */
        public String sessionName = "_Thor_Session";
        /**
         * positive value: session will be expired after a period time.
         * zero value: never expire except close the browser.
         * negative value: never expire regardless of whether the browser is closed but
         * session will be expired when time passed reach to the value of sessionDays.
         */
        public int sessionTimeout = 900;
        /**
         * If session is client session and set never expire then use this value to
         * specify how many days this session will be kept in client computer.
         */
        public int sessionDays = 90;
        /**
         * If different applications will share the session but they placed under different secondary domains,
         * then they can specify main domain name to session to achieve the goal.
         * Only available when session type is client session.
         */
        public String sessionDomain = null;

        /**
         * If different applications but in same domain will share the session
         * they can specify path to session to achieve the goal.
         * Only available when session type is client session.
         */
        public String sessionPath = null;

        public String rootPath = null;

        public int maxUploadSize = UploadManager.DEFAULT_MAX_SIZE;
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

    private ServletContext servletContext;
    private List<RouterInfo> routers = null;
    private List<FilterInfo> filters = null;
    private Class<? extends WebSession> sessionType = ClientSession.class;
    protected Setting setting;

    public void run() {}

    public WebApplication() {
        WebApp appAnno = this.getClass().getAnnotation(WebApp.class);
        if (appAnno == null) {
            throw new RuntimeException(NO_APP_NAME_MESSAGE);
        }
        setName(appAnno.name());
        appDataEnv = appAnno.appDataEnv();
        setServices(appAnno.services());
        sessionType = appAnno.sessionType();
        configName = appAnno.configName();
        init();
        setting = configManager.get("/web", Setting.class, new Setting());
    }

    public final Class<? extends WebSession> getSessionType() {
        return sessionType;
    }

    @Override
    protected void onConfigChanged() {
        setting = configManager.get("/web", Setting.class, new Setting());
    }

    final void onStartup(ServletContext servletContext) throws ServletException {
        this.servletContext = servletContext;
        this.onStartup();
    }

    @Override
    public final void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public final synchronized void contextDestroyed(ServletContextEvent sce) {
        super.destroy();
    }

    public final ServletContext getServletContext() {
        return servletContext;
    }

    public final UploadManager createUploadManager(String uploadFolderName, boolean storeDetail) {
        File filePath = new File(getDataDir(uploadFolderName));
        return new UploadManager(filePath, storeDetail, setting.maxUploadSize);
    }

    public final UploadManager createUploadManager(String uploadFolderName) {
        return createUploadManager(uploadFolderName, true);
    }

    public final UploadManager createUploadManager() {
        return createUploadManager("upload", true);

    }

    public final List<RouterInfo> getRouters()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (routers == null) {
            routers = new LinkedList<>();
            WebApp webApp = this.getClass().getAnnotation(WebApp.class);
            if (webApp == null) {
                WebRouterBase inst = new WebBasicRouter(this);
                configManager.addChangeListener(inst);
                routers.add(new RouterInfo(new String[]{"/*"}, inst));
            } else {
                for (WebRouter router : webApp.routers()) {
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

    public final List<FilterInfo> getFilters()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        if (filters == null) {
            filters = new LinkedList<>();
            WebApp webApp = this.getClass().getAnnotation(WebApp.class);
            if (webApp == null) {
                WebFilterBase inst = new WebSecurityManager(this);
                configManager.addChangeListener(inst);
                filters.add(new FilterInfo(new String[]{"/*"}, inst));
            } else {
                for (WebFilter filter : webApp.filters()) {
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

    public static <T> T createInstance(Class<T> clazz, WebApplication application)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
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

    public String getRootPath(HttpServletRequest request) {
        if (setting.rootPath == null) {
            return request.getContextPath();
        } else {
            return setting.rootPath;
        }
    }
}
