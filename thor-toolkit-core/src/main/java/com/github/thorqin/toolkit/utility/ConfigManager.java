package com.github.thorqin.toolkit.utility;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.joda.time.DateTime;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by nuo.qin on 12/11/2014.
 */
public class ConfigManager extends JsonAccessor {
    public interface ChangeListener {
        void onConfigChanged(ConfigManager configManager);
    }

    private Set<ChangeListener> listenerSet = new HashSet<>();
    private File file = null;
    private FileMonitor.Monitor monitor = null;
    private String rawContent = null;
    private JsonElement defaultRoot = null;

    private FileMonitor.FileChangeListener listener = new FileMonitor.FileChangeListener() {
        @Override
        public void onFileChange(File file, FileMonitor.ChangeType changeType, Object param) {
            String oldHash = getHash();
            try {
                Thread.sleep(100);
                loadFile();
            } catch (InterruptedException e) {
            }
            if (oldHash.equals(getHash()))
                return;
            for (ChangeListener listener: listenerSet) {
                try {
                    listener.onConfigChanged(ConfigManager.this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    public boolean isWatching() {
        return monitor != null;
    }

    public void addChangeListener(ChangeListener listener) {
        listenerSet.add(listener);
    }
    public void removeChangeListener(ChangeListener listener) {
        listenerSet.remove(listener);
    }


    public ConfigManager(String dataDir, String configFileName) throws IOException {
        load(dataDir, configFileName);
    }

    /**
     * Load config file either from data dir or from application resource dir,
     * This method let developer can load default setting from resource dir and
     * monitor data dir change, if new configuration file created in data dir
     * config manager will receive a notify.
     * @param dataDir Configurable directory path
     * @param configFileName Config file name if load from resource, then it be a resource file name
     */
    public synchronized void load(String dataDir, String configFileName) {
        stopWatch();
        rawContent = null;
        rootObj = null;
        file = null;
        loadDefaultResource(configFileName);
        if (dataDir == null) {
            merge();
            return;
        }
        dataDir = dataDir.replace('\\', '/');
        String path = (dataDir.endsWith("/") ? dataDir : dataDir + "/") + configFileName;
        file = new File(path);
        loadFile();
    }

    private synchronized void loadFile() {
        rawContent = null;
        rootObj = null;
        if (file != null && file.isFile()) {
            try {
                rawContent = Serializer.readTextFile(file);
            } catch (Exception ex) {
                rawContent = null;
                System.err.println("Load configuration file error: " + file + ": " + ex.getMessage());
            }
        }
        merge();
    }

    private void loadDefaultResource(String resource) {
        String textContent;
        try {
            textContent = Serializer.readTextResource(resource);
        } catch (IOException ex) {
            defaultRoot = null;
            return;
        }
        if (resource.matches(".+\\.yml")) {
            Object obj = Serializer.fromYaml(textContent);
            defaultRoot = Serializer.toJsonElement(obj);
        } else
            defaultRoot = Serializer.fromJson(textContent, JsonElement.class);
    }

    public synchronized void startWatch() {
        if (file == null || monitor != null)
            return;
        try {
            monitor = FileMonitor.watch(file, listener);
        } catch (Exception ex) {
            monitor = null;
            System.err.println("Watch configuration file error: " + file + ": " + ex.getMessage());
        }
    }

    public synchronized void stopWatch() {
        if (isWatching()) {
            monitor.close();
            monitor = null;
        }
    }


    private static void margeChild(JsonElement parent, JsonElement defaultObj, JsonElement newObj, String key) {
        // Both defaultObj and newObj must not be null.
        JsonElement result;
        if (newObj.isJsonArray() && defaultObj.isJsonArray()) {
            JsonArray resultArray = new JsonArray();
            JsonArray newArray = newObj.getAsJsonArray();
            JsonArray defaultArray = defaultObj.getAsJsonArray();
            int i = 0;
            for (; i < newArray.size(); i++) {
                if (i < defaultArray.size()) {
                    margeChild(resultArray, defaultArray.get(i), newArray.get(i), null);
                } else
                    resultArray.add(newArray.get(i));
            }
            for (; i < defaultArray.size(); i++) {
                resultArray.add(defaultArray.get(i));
            }
            result = resultArray;
        } else if (newObj.isJsonObject() && defaultObj.isJsonObject()) {
            JsonObject resultObject = new JsonObject();
            JsonObject newObject = newObj.getAsJsonObject();
            JsonObject defaultObject = defaultObj.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry: newObject.entrySet()) {
                String k = entry.getKey();
                if (defaultObject.has(k)) {
                    margeChild(resultObject, defaultObject.get(k), entry.getValue(), k);
                } else {
                    resultObject.add(k, entry.getValue());
                }
            }
            for (Map.Entry<String, JsonElement> entry: defaultObject.entrySet()) {
                if (!newObject.has(entry.getKey())) {
                    resultObject.add(entry.getKey(), entry.getValue());
                }
            }
            result = resultObject;
        } else
            result = newObj;
        if (parent.isJsonObject()) {
            parent.getAsJsonObject().add(key, result);
        } else if (parent.isJsonArray()) {
            parent.getAsJsonArray().add(result);
        }
    }

    private void merge() {
        JsonElement newRoot;
        if (rawContent == null) {
            newRoot = null;
        } else {
            if (file != null && file.getName().matches(".+\\.yml")) {
                Object obj = Serializer.fromYaml(rawContent);
                newRoot = Serializer.toJsonElement(obj);
            } else
                newRoot = Serializer.fromJson(rawContent, JsonElement.class);
        }
        if (defaultRoot == null) {
            rootObj = newRoot;
        } else if (newRoot == null) {
            rootObj = defaultRoot;
        } else { // Both not null
            JsonArray container = new JsonArray();
            margeChild(container, defaultRoot, newRoot, null);
            rootObj = container.get(0);
            rawContent = Serializer.toJsonString(rootObj);
        }
    }



    public String getHash() {
        if (rawContent == null) {
            return Encryptor.md5String(new byte[0]);
        } else
            return Encryptor.md5String(rawContent.getBytes());
    }
}
