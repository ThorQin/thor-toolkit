package com.github.thorqin.toolkit.web.router;

import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.web.WebApplication;

import javax.servlet.http.HttpServlet;

/**
 * Created by nuo.qin on 1/28/2015.
 */
public abstract class WebRouterBase extends HttpServlet implements ConfigManager.ChangeListener {
    protected final WebApplication application;

    public WebRouterBase(WebApplication application) {
        this.application = application;
    }

    public WebApplication getApplication() {
        return application;
    }

    @Override
    public void onConfigChanged(ConfigManager configManager) {
    }
}
