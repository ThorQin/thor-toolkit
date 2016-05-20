package com.github.thorqin.toolkit.web.taglib;

import com.github.thorqin.toolkit.utility.Localization;
import com.google.common.base.Strings;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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
                String lang = null;
                for (Cookie cookie : request.getCookies()) {
                    String name = cookie.getName();
                    if (name != null && name.equals("tt-lang")) {
                        lang = cookie.getValue();
                        break;
                    }
                }
                if (lang != null)
                    currentLocale = lang;
                else
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
        if (Strings.isNullOrEmpty(appName))
            appName = null;
        this.appName = appName;
    }
}
