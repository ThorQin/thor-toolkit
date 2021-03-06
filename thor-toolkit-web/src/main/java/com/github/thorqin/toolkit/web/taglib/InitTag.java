package com.github.thorqin.toolkit.web.taglib;

import com.github.thorqin.toolkit.Application;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.session.SessionFactory;
import com.github.thorqin.toolkit.web.session.WebSession;
import com.google.common.base.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Created by thor on 6/12/15.
 */
public class InitTag extends SimpleTagSupport {
    private String bundle = null;
    private String locale = null;
    private String appName = null;

    @Override
    public void doTag() throws JspException, IOException {
        HttpServletRequest request = (HttpServletRequest)this.getJspContext()
                .getAttribute("javax.servlet.jsp.jspRequest");
        HttpServletResponse response = (HttpServletResponse)this.getJspContext()
                .getAttribute("javax.servlet.jsp.jspResponse");

        WebApplication app = (WebApplication) Application.get(appName);
        WebSession session = null;
        if (app != null) {
            try {
                SessionFactory sessionFactory = new SessionFactory();
                sessionFactory.setSessionType(app.getSessionType());
                session = sessionFactory.getSession(app, request, response);
                getJspContext().setAttribute("session", session);
            } catch (ServletException e) {
                throw new JspException(e);
            }
        }
        String rootPath;
        if (app != null)
            rootPath = app.getRootPath(request);
        else {
            rootPath = request.getContextPath();
        }
        getJspContext().setAttribute("root", rootPath);
        String relative = "";
        String referencePath = (String)request.getAttribute("_thor_toolkit_redirect_reference_path_");
        String p;
        if (referencePath != null)
            p = referencePath.replaceAll("/+", "/").replaceAll("/./","/");
        else
            p = request.getRequestURI().replaceAll("/+", "/").replaceAll("/./","/");
        int pos = 0;
        int count = 0;
        while (pos < p.length()) {
            if (p.charAt(pos) == '/') {
                count++;
                if (count > 2) {
                    relative += "/";
                }
                if (count > 1) {
                    relative += "..";
                }
            }
            pos++;
        }
        getJspContext().setAttribute("relative", relative);

        String currentLocale;
        if (locale == null) {
            String lang = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    String name = cookie.getName();
                    if (name != null && name.equals("tt-lang")) {
                        lang = cookie.getValue();
                        break;
                    }
                }
            }
            if (lang != null)
                currentLocale = lang;
            else
                currentLocale = request.getHeader("Accept-Language");
        } else
            currentLocale = locale;
        getJspContext().setAttribute("lang", Localization.standardize(currentLocale));
        if (bundle == null) {
            getJspContext().setAttribute("loc", Localization.getInstance());
        } else {
            getJspContext().setAttribute("loc", Localization.getInstance(bundle, currentLocale));
        }
    }

    public void setAppName(String appName) {
        if (Strings.isNullOrEmpty(appName))
            appName = null;
        this.appName = appName;
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
}
