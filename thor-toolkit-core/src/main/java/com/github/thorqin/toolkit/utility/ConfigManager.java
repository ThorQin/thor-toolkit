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
public class ConfigManager {
    public interface ChangeListener {
        void onConfigChanged(ConfigManager configManager);
    }

    private Set<ChangeListener> listenerSet = new HashSet<>();
    private File file = null;
    private FileMonitor.Monitor monitor = null;
    private JsonElement rootObj = null;
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


    private void margeChild(JsonElement parent, JsonElement defaultObj, JsonElement newObj, String key) {
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

    private JsonElement getFromRoot(String jsonPath, JsonElement root) {
        String[] paths = jsonPath.split("(?<!\\\\)/");
        JsonElement obj = root;
        for (String p : paths) {
            if (obj != null) {
                p = p.replace("\\/", "/");
                if (p == null || p.isEmpty()) {
                    continue;
                }
                if (obj.isJsonObject()) {
                    obj = ((JsonObject)obj).get(p);
                } else if (obj.isJsonArray()) {
                    try {
                        int idx = Integer.parseInt(p);
                        obj = ((JsonArray)obj).get(idx);
                    } catch (Exception ex) {
                        return null;
                    }
                } else
                    return null;
            } else {
                return null;
            }
        }
        return obj;
    }

    /**
     * Get the value which is indicated by the json path.
     * @param jsonPath JSON path to indicate where we should extract info from the configuration.
     * @return JsonElement depends on which type of the JSON path pointed to.
     */
    public JsonElement get(String jsonPath) {
        return getFromRoot(jsonPath, rootObj);
    }

    public <T> T get(String jsonPath, Class<T> type) {
        JsonElement obj = get(jsonPath);
        if (obj != null)
            return Serializer.fromJson(obj, type);
        else
            return null;
    }

    public <T> T get(String jsonPath, Class<T> type, T defaultValue) {
        try {
            T value = get(jsonPath, type);
            return (value != null ? value : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String getString(String jsonPath) {
        return get(jsonPath, String.class);
    }

    public String getString(String jsonPath, String defaultValue) {
        return get(jsonPath, String.class, defaultValue);
    }

    public Integer getInteger(String jsonPath) {
        return get(jsonPath, Integer.class);
    }

    public Integer getInteger(String jsonPath, Integer defaultValue) {
        return get(jsonPath, Integer.class, defaultValue);
    }

    public Long getLong(String jsonPath) {
        return get(jsonPath, Long.class);
    }

    public Long getLong(String jsonPath, Long defaultValue) {
        return get(jsonPath, Long.class, defaultValue);
    }

    public Short getShort(String jsonPath) {
        return get(jsonPath, Short.class);
    }

    public Short getShort(String jsonPath, Short defaultValue) {
        return get(jsonPath, Short.class, defaultValue);
    }

    public Byte getByte(String jsonPath) {
        return get(jsonPath, Byte.class);
    }

    public Byte getByte(String jsonPath, Byte defaultValue) {
        return get(jsonPath, Byte.class, defaultValue);
    }

    public BigInteger getBigInteger(String jsonPath) {
        return get(jsonPath, BigInteger.class);
    }

    public BigInteger getBigInteger(String jsonPath, BigInteger defaultValue) {
        return get(jsonPath, BigInteger.class, defaultValue);
    }

    public BigDecimal getBigDecimal(String jsonPath) {
        return get(jsonPath, BigDecimal.class);
    }

    public BigDecimal getBigDecimal(String jsonPath, BigDecimal defaultValue) {
        return get(jsonPath, BigDecimal.class, defaultValue);
    }

    public Boolean getBoolean(String jsonPath) {
        return get(jsonPath, Boolean.class);
    }

    public Boolean getBoolean(String jsonPath, Boolean defaultValue) {
        return get(jsonPath, Boolean.class, defaultValue);
    }

    public Float getFloat(String jsonPath) {
        return get(jsonPath, Float.class);
    }

    public Float getFloat(String jsonPath, Float defaultValue) {
        return get(jsonPath, Float.class, defaultValue);
    }

    public Double getDouble(String jsonPath) {
        return get(jsonPath, Double.class);
    }

    public Double getDouble(String jsonPath, Double defaultValue) {
        return get(jsonPath, Double.class, defaultValue);
    }

    public DateTime getDateTime(String jsonPath) {
        String str = getString(jsonPath);
        if (str != null) {
            return StringUtils.parseISO8601(str);
        } else
            return null;
    }

    public DateTime getDateTime(String jsonPath, DateTime defaultValue) {
        try {
            DateTime val = getDateTime(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public <T> List<T> getList(String jsonPath, Class<T> type) {
        JsonElement obj = get(jsonPath);
        if (obj != null) {
            if (obj.isJsonArray()) {
                JsonArray array = (JsonArray)obj;
                List<T> result = new ArrayList<>(array.size());
                for (int i = 0; i <array.size(); i++) {
                    result.add(Serializer.fromJson(array.get(i), type));
                }
                return result;
            } else
                return new ArrayList<>();
        } else
            return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String jsonPath, Type type) {
        JsonElement obj = get(jsonPath);
        if (obj != null) {
            if (obj.isJsonArray()) {
                JsonArray array = (JsonArray)obj;
                List<T> result = new ArrayList<>(array.size());
                for (int i = 0; i <array.size(); i++) {
                    result.add((T)Serializer.fromJson(array.get(i), type));
                }
                return result;
            } else
                return new ArrayList<>();
        } else
            return new ArrayList<>();
    }

    public List<?> getList(String jsonPath) {
        JsonElement obj = get(jsonPath);
        if (obj != null) {
            if (obj.isJsonArray()) {
                return Serializer.fromJson(obj, List.class);
            } else
                return new ArrayList<>();
        } else
            return new ArrayList<>();
    }

    public <T> Map<String, T> getMap(String jsonPath, Class<T> type) {
        JsonElement obj = get(jsonPath);
        if (obj != null) {
            if (obj.isJsonObject()) {
                JsonObject jsonObj = (JsonObject)obj;
                Map<String, T> result = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry: jsonObj.entrySet()) {
                    result.put(entry.getKey(), Serializer.fromJson(entry.getValue(), type));
                }
                return result;
            } else
                return new HashMap<>();
        } else
            return new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getMap(String jsonPath, Type type) {
        JsonElement obj = get(jsonPath);
        if (obj != null) {
            if (obj.isJsonObject()) {
                JsonObject jsonObj = (JsonObject)obj;
                Map<String, T> result = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry: jsonObj.entrySet()) {
                    result.put(entry.getKey(), (T)Serializer.fromJson(entry.getValue(), type));
                }
                return result;
            } else
                return new HashMap<>();
        } else
            return new HashMap<>();
    }

    public Map<String, Object> getMap(String jsonPath) {
        JsonElement obj = get(jsonPath);
        if (obj != null) {
            if (obj.isJsonObject()) {
                Type type = new TypeToken<Map<String, Object>>() {}.getType();
                return Serializer.fromJson(obj, type);
            } else
                return new HashMap<>();
        } else
            return new HashMap<>();
    }

    public String getJson(String jsonPath, boolean prettyPrint) {
        JsonElement obj = get(jsonPath);
        return Serializer.toJsonString(obj, prettyPrint);
    }

    public String getJson(String jsonPath) {
        JsonElement obj = get(jsonPath);
        return Serializer.toJsonString(obj, false);
    }

    public String getHash() {
        if (rawContent == null) {
            return Encryptor.md5String(new byte[0]);
        } else
            return Encryptor.md5String(rawContent.getBytes());
    }
}
