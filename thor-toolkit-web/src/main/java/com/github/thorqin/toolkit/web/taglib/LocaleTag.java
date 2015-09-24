package com.github.thorqin.toolkit.web.taglib;

import com.github.thorqin.toolkit.Application;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.session.SessionFactory;
import com.github.thorqin.toolkit.web.session.WebSession;
import com.google.common.base.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Created by thor on 6/12/15.
 */
public class LocaleTag extends SimpleTagSupport {

    private String bundle = null;
    private String locale = null;
    private String appName = null;

    @Override
    public void doTag() throws JspException, IOException {
        HttpServletRequest request = (HttpServletRequest)this.getJspContext()
                .getAttribute("javax.servlet.jsp.jspRequest");
        if (bundle == null) {
            getJspContext().setAttribute("loc", Localization.getInstance());
        } else {
            String currentLocale;
            if (locale == null) {
                WebApplication app = (WebApplication) Application.get(appName);
                if (app != null) {
                    try {
                        HttpServletResponse response = (HttpServletResponse)this.getJspContext()
                                .getAttribute("javax.servlet.jsp.jspResponse");
                        SessionFactory sessionFactory = new SessionFactory();
                        sessionFactory.setSessionType(app.getSessionType());
                        WebSession session = sessionFactory.getSession(app, request, response);
                        Object lang = session.get("lang");
                        if (lang != null && lang.getClass().equals(String.class))
                            currentLocale = (String)lang;
                        else
                            currentLocale = request.getHeader("Accept-Language");
                    } catch (ServletException e) {
                        throw new JspException(e);
                    }
                } else
                    currentLocale = request.getHeader("Accept-Language");
            } else
                currentLocale = locale;
            getJspContext().setAttribute("loc", Localization.getInstance(bundle, currentLocale));
        }
    }

    public void setBundle(String bundle) {
        if (Strings.isNullOrEmpty(bundle))
            bundle = null;
        this.bundle = bundle;
    }

    public void setLocale(String locale) {
        if (Strings.isNullOrEmpty(locale))
            locale = null;
        this.locale = locale;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
