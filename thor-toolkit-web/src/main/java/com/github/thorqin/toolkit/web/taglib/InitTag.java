package com.github.thorqin.toolkit.web.taglib;

import com.github.thorqin.toolkit.web.WebApplication;
import com.github.thorqin.toolkit.web.session.SessionFactory;
import com.github.thorqin.toolkit.web.session.WebSession;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Created by thor on 6/12/15.
 */
public class InitTag extends SimpleTagSupport {

    private String appName = null;

    @Override
    public void doTag() throws JspException, IOException {
        HttpServletRequest request = (HttpServletRequest)this.getJspContext()
                .getAttribute("javax.servlet.jsp.jspRequest");
        HttpServletResponse response = (HttpServletResponse)this.getJspContext()
                .getAttribute("javax.servlet.jsp.jspResponse");

        WebApplication app = WebApplication.get(appName);
        if (app != null) {
            try {
                SessionFactory sessionFactory = new SessionFactory();
                sessionFactory.setSessionType(app.getSessionType());
                WebSession session = sessionFactory.getSession(app, request, response);
                getJspContext().setAttribute("session", session);
            } catch (ServletException e) {
                throw new JspException(e);
            }
        }
        getJspContext().setAttribute("root", request.getContextPath());
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
