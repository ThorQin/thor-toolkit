package com.github.thorqin.toolkit.utility;

import com.github.thorqin.toolkit.Application;

import java.io.File;
import java.io.IOException;

/**
 * Created by thor on 2015/9/19.
 */
public class AppConfigManager extends ConfigManager {
    protected Application application = null;
    public AppConfigManager(Application application, String configName) throws IOException {
        this.application = application;
        this.load(application.getDataDir(), configName);
    }

    public Application getApplication() {
        return this.application;
    }

    public String getDataDir() {
        return application.getDataDir();
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
