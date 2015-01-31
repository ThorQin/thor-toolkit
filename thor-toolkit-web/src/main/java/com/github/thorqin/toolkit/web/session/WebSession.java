package com.github.thorqin.toolkit.web.session;

import com.github.thorqin.toolkit.web.WebApplication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Enumeration;

/**
 * Created by nuo.qin on 1/28/2015.
 */
public abstract class WebSession {
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected WebApplication application;

    public WebSession(WebApplication application, HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.application = application;
    }
    public abstract Enumeration<String> getKeys();
    public abstract Object get(String key);
    public abstract void set(String key, Object value);
    public abstract void remove(String key);
    public abstract void clear();
    public abstract String getId();
    public abstract long getCreationTime();
    public abstract long getLastAccessedTime();
    public abstract void save();
    public abstract void touch();
    public boolean isExpired() {
        if (this.getMaxInactiveInterval() <= 0)
            return false;
        else
            return (new Date().getTime() - getLastAccessedTime() > getMaxInactiveInterval() * 1000);
    }
    public abstract boolean isNew();
    public abstract boolean isSaved();
    public abstract void setMaxInactiveInterval(int interval);
    public abstract int getMaxInactiveInterval();
}
