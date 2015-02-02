/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.web.session.SessionFactory;
import com.github.thorqin.toolkit.web.utility.ServletUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author nuo.qin
 */
public class WebSecurityManager extends WebFilterBase {
    private static final Logger logger = Logger.getLogger(WebBasicRouter.class.getName());

    public static class SecuritySetting {
        public boolean defaultAllow = true;
        public List<Map<String, Object>> rules = new ArrayList<>();
    }

    private SecuritySetting setting = null;

	private SessionFactory sessionFactory = new SessionFactory();

	public WebSecurityManager(WebApplication application) {
		super(application);
	}

	public WebSecurityManager() {
		super(null);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		String sessionTypeName = config.getInitParameter("sessionClass");
		sessionFactory.setSessionType(sessionTypeName);
        setting = application.getConfigManager().get("/", SecuritySetting.class);
	}

    @Override
    public void onConfigChanged(ConfigManager config) {
        setting = application.getConfigManager().get("/", SecuritySetting.class);
    }

    private Object checkPermission(HttpServletRequest request, HttpServletResponse response) {
        if (setting == null)
            return false;
        try {
            for (Map<String, Object> rule : setting.rules) {
                for (String key: rule.keySet()) {
                    Object value = rule.get(key);
                    if (key.equals("uri")) {

                    } else if (key.equals("method")) {

                    } else if (key.equals("server")) {

                    } else if (key.equals("port")) {

                    } else if (key.equals("schema")) {

                    } else if (key.startsWith("s:")) { // Session value

                    } else if (key.startsWith("h:")) { // Header value

                    } else if (key.startsWith("q:")) { // Query string value

                    }
                }
                // Matched

            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Invalid security configuration.", ex);
            return false;
        }
        return setting.defaultAllow;
    }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		long beginTime = System.currentTimeMillis();
        HttpServletRequest req = (HttpServletRequest)request;
        boolean allow = false;
        try {
            Object action = checkPermission(req, (HttpServletResponse)response);
            if (action.getClass().equals(boolean.class)) {
                allow = (boolean)action;
                if (allow)
                    chain.doFilter(request, response);
                else
                    ServletUtils.sendText((HttpServletResponse)response, HttpServletResponse.SC_FORBIDDEN, "Forbidden!");
            } else if (action.getClass().equals(String.class)) {
                allow = false;
                String redirection = (String)action;
                ((HttpServletResponse) response).sendRedirect(redirection);
            } else {
                allow = false;
                ServletUtils.sendText((HttpServletResponse)response, HttpServletResponse.SC_FORBIDDEN, "Forbidden!");
            }
        } catch (Exception ex) {
            ServletUtils.sendText((HttpServletResponse)response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error!");
            logger.log(Level.SEVERE, "Error processing", ex);
        } finally {
            if (application != null && application.getSetting().traceSecurity) {
                Tracer.Info traceInfo = new Tracer.Info();
                traceInfo.catalog = "security";
                traceInfo.name = "access";
                traceInfo.put("url", ServletUtils.getURL(req));
                traceInfo.put("method", req.getMethod().toUpperCase());
                traceInfo.put("clientIP", req.getRemoteAddr());
                traceInfo.put("allowed", allow);
                traceInfo.put("startTime", beginTime);
                traceInfo.put("runningTime", System.currentTimeMillis() - beginTime);
                application.trace(traceInfo);
            }
        }
	}

	@Override
	public void destroy() {
	}
	
}
