package com.github.thorqin.toolkit.web.session;

import com.github.thorqin.toolkit.web.WebApplication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nuo.qin on 1/28/2015.
 */
public class ServerSession extends WebSession {
    public ServerSession(WebApplication application, HttpServletRequest request, HttpServletResponse response) {
        super(application, request, response);
        HttpSession session = request.getSession(true);
        if (session.isNew() && application != null)
            session.setMaxInactiveInterval(application.getSetting().sessionTimeout);
    }

    @Override
    public Enumeration<String> getKeys() {
        return request.getSession().getAttributeNames();
    }

    @Override
    public Map<String, Object> getMap() {
        Map<String, Object> values = new HashMap<>();
        for (Enumeration<String> keys = getKeys(); keys.hasMoreElements();) {
            String key = keys.nextElement();
            values.put(key, get(key));
        }
        return values;
    }

    @Override
    public Object get(String key) {
        return request.getSession().getAttribute(key);
    }

    @Override
    public void set(String key, Object value) {
        request.getSession().setAttribute(key, value);
    }

    @Override
    public void remove(String key) {
        request.getSession().removeAttribute(key);
    }

    @Override
    public void clear() {
        request.getSession().invalidate();
    }

    @Override
    public String getId() {
        return request.getSession().getId();
    }

    @Override
    public long getCreationTime() {
        return request.getSession().getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return request.getSession().getLastAccessedTime();
    }

    @Override
    public void save() {
    }

    @Override
    public void touch() {
        request.getSession().getId();
    }

    @Override
    public boolean isSaved() {
        return true;
    }

    @Override
    public boolean isNew() {
        return request.getSession().isNew();
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        request.getSession().setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        return request.getSession().getMaxInactiveInterval();
    }
}
