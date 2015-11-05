package com.github.thorqin.toolkit.web.taglib;

import com.github.thorqin.toolkit.Application;
import com.github.thorqin.toolkit.web.WebApplication;
import com.google.common.base.Strings;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Created by thor on 11/5/15.
 */
public class ServiceTag extends SimpleTagSupport {
    private String serviceName = null;
    private String appName = null;
    private String defineName = null;

    @Override
    public void doTag() throws JspException, IOException {
        WebApplication app = (WebApplication) Application.get(appName);
        Object service = app.getService(serviceName);
        getJspContext().setAttribute(defineName == null ? serviceName : defineName, service);
    }

    public void setService(String service) {
        if (Strings.isNullOrEmpty(service))
            throw new RuntimeException("Must specify service name to use it.");
        this.serviceName = service;
    }

    public void setName(String name) {
        if (Strings.isNullOrEmpty(name))
            name = null;
        this.defineName = name;
    }

    public void setAppName(String appName) {
        if (Strings.isNullOrEmpty(appName))
            appName = null;
        this.appName = appName;
    }
}
