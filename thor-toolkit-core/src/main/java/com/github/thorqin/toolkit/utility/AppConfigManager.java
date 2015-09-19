package com.github.thorqin.toolkit.utility;

import java.io.File;
import java.io.IOException;

/**
 * Created by thor on 2015/9/19.
 */
public class AppConfigManager extends ConfigManager {
    protected String appName = null;
    protected String environmentValueName = null;
    public AppConfigManager(String appName, String configName) throws IOException {
        this.appName = appName;
        this.environmentValueName = null;
        this.load(ConfigManager.getAppDataDir(appName), configName);
    }

    public AppConfigManager(String environmentValueName, String appName, String configName) throws IOException {
        this.appName = appName;
        this.environmentValueName = environmentValueName;
        this.load(ConfigManager.getAppDataDir(environmentValueName, appName), configName);
    }

    public String getAppName() {
        return this.appName;
    }

    public String getDataDir() {
        return ConfigManager.getAppDataDir(null, appName);
    }

    /**
     * Load file from APP_DATA or class loader directory if file not existing in APP_DATA directory.
     * @param fileName file name, may contain sub directory part of the full path.
     * @param encoding file encoding.
     * @return text content.
     * @throws IOException
     */
    public String readAppTextFile(String fileName, String encoding) throws IOException {
        File file = new File(getDataDir() + "/" + fileName);
        if (file.exists()) {
            return Serializer.readTextFile(file, encoding);
        } else {
            return Serializer.readTextResource(fileName, encoding);
        }
    }

    /**
     * Load file from APP_DATA or class loader directory if file not existing in APP_DATA directory.
     * @param fileName file name, may contain sub directory part of the full path.
     * @return text content, text encoding will be auto detected.
     * @throws IOException
     */
    public String readAppTextFile(String fileName) throws IOException {
        File file = new File(getDataDir() + "/" + fileName);
        if (file.exists()) {
            return Serializer.readTextFile(file);
        } else {
            return Serializer.readTextResource(fileName);
        }
    }

    public byte[] readAppFile(String fileName) throws IOException {
        File file = new File(getDataDir() + "/" + fileName);
        if (file.exists()) {
            return Serializer.readFile(file);
        } else {
            return Serializer.readResource(fileName);
        }
    }
}
