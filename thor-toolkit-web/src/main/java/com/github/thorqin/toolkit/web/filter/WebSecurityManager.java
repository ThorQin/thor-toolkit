/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.web.filter;

import com.github.thorqin.toolkit.trace.Tracer;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.session.SessionFactory;
import com.github.thorqin.toolkit.web.session.WebSession;
import com.github.thorqin.toolkit.web.utility.ServletUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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
    private final Logger logger;

    public static class SecuritySetting {
        public boolean defaultAllow = true;
        public List<Map<String, Object>> accessRules = new ArrayList<>();
    }

    private static class RuleAction {
        public boolean allow = false;
        public String redirection = null;
    }

    private SecuritySetting setting = null;
    private List<Map<String, Pattern>> rules = new ArrayList<>();
    private List<RuleAction> actions = new ArrayList<>();
    private RuleAction defaultAction = new RuleAction();

	private SessionFactory sessionFactory = new SessionFactory();

	public WebSecurityManager(WebApplication application) {
		super(application);
        logger = application.getLogger();
	}

	public WebSecurityManager() {
		super(null);
        logger = Logger.getLogger(WebSecurityManager.class.getName());
    }

    private void buildSetting() {
        List<Map<String, Pattern>> newRules = new ArrayList<>();
        List<RuleAction> newActions = new ArrayList<>();
        if (setting == null) {
            return;
        }
        defaultAction.allow = setting.defaultAllow;
        if (setting.accessRules == null)
            return;
        for (Map<String, Object> rule: setting.accessRules) {
            Map<String, Pattern> newRule = new HashMap<>();
            RuleAction action = new RuleAction();
            action.allow = !setting.defaultAllow;
            action.redirection = null;
            for (String key: rule.keySet()) {
                if (key.equals("action")) {
                    Object value = rule.get(key);
                    if (value != null) {
                        if (value.getClass().equals(boolean.class) ||
                                value.getClass().equals(Boolean.class)) {
                            action.allow = (boolean)value;
                        } else if (value.getClass().equals(String.class)) {
                            action.allow = false;
                            action.redirection = (String)value;
                        }
                    }
                    continue;
                }
                String value = (String)rule.get(key);
                Pattern pattern = Pattern.compile(value);
                newRule.put(key, pattern);
            }
            newRules.add(newRule);
            newActions.add(action);
        }
        rules = newRules;
        actions = newActions;
    }

	@Override
	public void init(FilterConfig config) throws ServletException {
		String sessionTypeName = config.getInitParameter("sessionClass");
		sessionFactory.setSessionType(sessionTypeName);
        setting = application.getConfigManager().get("/", SecuritySetting.class, null);
        buildSetting();
	}

    @Override
    public synchronized void onConfigChanged(ConfigManager configManager) {
        setting = application.getConfigManager().get("/", SecuritySetting.class);
        buildSetting();
    }

    private synchronized RuleAction checkPermission(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        if (setting == null)
            return new RuleAction();
        WebSession session = null;
        Map<String, String> queryString = null;
        try {
            MATCHE_RULE:
            for (int i = 0; i < rules.size(); i++) {
                Map<String, Pattern> rule = rules.get(i);
                for (String key: rule.keySet()) {
                    Pattern pattern = rule.get(key);
                    if (key.equals("path")) {
                        String path = request.getServletPath();
                        if (request.getPathInfo() != null)
                            path += request.getPathInfo();
                        if (!pattern.matcher(path).matches()) {
                            continue MATCHE_RULE;
                        }
                    } else if (key.equals("method")) {
                        if (!pattern.matcher(request.getMethod().toUpperCase()).matches()) {
                            continue MATCHE_RULE;
                        }
                    } else if (key.startsWith("s:")) { // Session value
                        if (session == null) {
                            session = sessionFactory.getSession(application, request, response);
                        }
                        Object value = session.get(key.substring(2));
                        String valStr = value == null ? "" : value.toString();
                        if (!pattern.matcher(valStr).matches()) {
                            continue MATCHE_RULE;
                        }
                    } else if (key.equals("server")) {
                        if (!pattern.matcher(request.getServerName()).matches()) {
                            continue MATCHE_RULE;
                        }
                    } else if (key.equals("scheme")) {
                        if (!pattern.matcher(request.getScheme()).matches()) {
                            continue MATCHE_RULE;
                        }
                    } else if (key.equals("port")) {
                        if (!pattern.matcher(String.valueOf(request.getServerPort())).matches()) {
                            continue MATCHE_RULE;
                        }
                    } else if (key.equals("client")) {
                        if (!pattern.matcher(request.getRemoteAddr()).matches()) {
                            continue MATCHE_RULE;
                        }
                    } else if (key.equals("uri")) {
                        if (!pattern.matcher(request.getRequestURI()).matches()) {
                            continue MATCHE_RULE;
                        }
                    } else if (key.startsWith("h:")) { // Header value
                        Object value = request.getHeader(key.substring(2));
                        String valStr = value == null ? "" : value.toString();
                        if (!pattern.matcher(valStr).matches()) {
                            continue MATCHE_RULE;
                        }
                    } else if (key.startsWith("q:")) { // Query string value
                        if (queryString == null) {
                            queryString = Serializer.fromUrlEncoding(request.getQueryString());
                        }
                        String value = queryString.get(key.substring(2));
                        if (!pattern.matcher(value).matches()) {
                            continue MATCHE_RULE;
                        }
                    }
                }
                // Matched
                return actions.get(i);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Invalid security configuration.", ex);
            return new RuleAction();
        }
        return defaultAction;
    }

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		long beginTime = System.currentTimeMillis();
        HttpServletRequest req = (HttpServletRequest)request;
        boolean allow = false;
        try {
            RuleAction action = checkPermission(req, (HttpServletResponse)response);
            allow = action.allow;
            if (action.allow) {
                chain.doFilter(request, response);
            } else if (action.redirection == null) {
                ServletUtils.sendText((HttpServletResponse)response, HttpServletResponse.SC_FORBIDDEN, "Forbidden!");
            } else {
                HttpServletResponse resp = ((HttpServletResponse)response);
                resp.sendRedirect(action.redirection);
            }
        } catch (Exception ex) {
            ServletUtils.sendText((HttpServletResponse)response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error!");
            logger.log(Level.SEVERE, "Error processing", ex);
        } finally {
            if (application != null && application.getSetting().traceAccess) {
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
