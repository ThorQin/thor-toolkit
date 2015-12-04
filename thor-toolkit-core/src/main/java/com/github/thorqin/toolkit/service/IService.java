package com.github.thorqin.toolkit.service;

import com.github.thorqin.toolkit.utility.ConfigManager;

/**
 * Created by thor on 11/30/15.
 */
public interface IService {
    /**
     * Framework will invoke this method after instance created and before invoke the 'config' method.
     */
    void start();

    /**
     * Framework will invoke this method before application shutdown,
     * or services reconfigured.
     */
    void stop();

    /**
     * Report to framework whether the service has been started.
     * @return True if service has been started.
     */
    boolean isStarted();

    /**
     * Framework will invoke this method after instance created or any configuration changed,
     * service implements should decide whether or not re-initialize the service by itself.
     * @param configManager Config manager object used to get the settings from the config file.
     * @param serviceName Service instance name, typically used as configuration key.
     * @param isReConfig indicate whether is a re-configuration action
     * @return Return TRUE indicate service should be restarted, FALSE is not.
     */
    boolean config(ConfigManager configManager, String serviceName, boolean isReConfig);
}
