package com.github.thorqin.toolkit.web.taglib;

import com.github.thorqin.toolkit.utility.Localization;
import com.google.common.base.Strings;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

/**
 * Created by thor on 6/12/15.
 */
public class InitTag extends SimpleTagSupport {

    private String bundle = null;
    private String locale = null;

    @Override
    public void doTag() throws JspException, IOException {
        HttpServletRequest request = (HttpServletRequest)this.getJspContext()
                .getAttribute("javax.servlet.jsp.jspRequest");
        getJspContext().setAttribute("root", request.getContextPath());

        if (bundle == null)
            return;

        String currentLocale;
        if (locale == null)
            currentLocale = request.getHeader("Accept-Language");
        else
            currentLocale = locale;
        getJspContext().setAttribute("loc", Localization.getInstance(bundle, currentLocale));
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
