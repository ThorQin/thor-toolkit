package com.github.thorqin.toolkit.web;

import com.github.thorqin.toolkit.utility.ConfigManager;

import javax.servlet.Filter;

/**
 * Created by nuo.qin on 1/28/2015.
 */
public abstract class WebFilterBase implements Filter, ConfigManager.ChangeListener {
    protected final WebApplication application;

    WebFilterBase(WebApplication application) {
        this.application = application;
    }

    public WebApplication getApplication() {
        return application;
    }

    @Override
    public void onConfigChanged(ConfigManager config) {
    }
}
