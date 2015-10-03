package com.github.thorqin.toolkit.web.session;

import com.github.thorqin.toolkit.web.WebApplication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Constructor;

/**
 * Created by nuo.qin on 1/28/2015.
 */
public class SessionFactory {
    private Class<? extends WebSession> sessionType = ClientSession.class;

    public WebSession getSession(WebApplication application, HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            Constructor<? extends WebSession> constructor = sessionType.getConstructor(
                    WebApplication.class, HttpServletRequest.class, HttpServletResponse.class);
            if (constructor == null) {
                throw new NoSuchMethodException("Invalid session constructor, see WebSession class definition.");
            }
            constructor.setAccessible(true);
            return constructor.newInstance(application, request, response);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
    @SuppressWarnings("unchecked")
    public void setSessionType(String sessionTypeName) throws ServletException {
        if (sessionTypeName != null && !sessionTypeName.isEmpty()) {
            Class<?> type;
            try {
                type = Class.forName(sessionTypeName);
            } catch (Exception ex) {
                throw new ServletException("Invalid session type.", ex);
            }
            setSessionType((Class<? extends WebSession>)type);
        } else
            throw new ServletException("Invalid session type, cannot be null or empty.");
    }

    public void setSessionType(Class<? extends WebSession> type) throws ServletException {
        try {
            if (WebSession.class.isAssignableFrom(type)) {
                Constructor<?> constructor = type.getConstructor(
                        WebApplication.class, HttpServletRequest.class, HttpServletResponse.class);
                if (constructor == null) {
                    throw new NoSuchMethodException("Invalid constructor, see WebSession class definition.");
                }
                sessionType = type;
            } else {
                throw new ServletException("Session class must inherit from WebSession.");
            }
        } catch (Exception ex) {
            throw new ServletException("Invalid session type.", ex);
        }
    }
}
