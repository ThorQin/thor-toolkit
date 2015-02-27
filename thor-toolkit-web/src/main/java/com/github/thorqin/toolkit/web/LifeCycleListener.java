package com.github.thorqin.toolkit.web;

import javax.servlet.ServletException;

/**
 * Created by nuo.qin on 1/27/2015.
 */
public interface LifeCycleListener {
    public void onStartup() throws ServletException;
    public void onShutdown();
}
