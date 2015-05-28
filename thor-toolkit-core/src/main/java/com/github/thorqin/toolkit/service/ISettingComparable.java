package com.github.thorqin.toolkit.service;

import com.github.thorqin.toolkit.utility.ConfigManager;

/**
 * Created by thor on 5/28/15.
 */
public interface ISettingComparable {
    boolean isSettingChanged(ConfigManager configManager, String configName);
}
