package com.github.thorqin.toolkit.utility;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.joda.time.DateTime;
import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nuo.qin on 12/11/2014.
 */
public class ConfigManager {
    public interface ChangeListener {
        void onConfigChanged(ConfigManager configManager);
    }

    private static WatchService monitorService = null;
    private static final Map<WatchKey, ConfigManager> monitoredMap = new HashMap<>();

    private File _file = null;
    private boolean monitorFileChange = false;
    private Set<ChangeListener> listenerSet = new HashSet<>();
    private WatchKey watchKey = null;
    private JsonElement rootObj = null;
    private String rawContent = null;
    private JsonElement defaultRoot = null;
    private String appName = null;

    private static synchronized void setWatchConfig(WatchKey key, ConfigManager config) {
        monitoredMap.put(key, config);
    }

    private static synchronized ConfigManager getWatchConfig(WatchKey key) {
        return monitoredMap.get(key);
    }

    private static synchronized void removeWatchConfig(WatchKey key) {
        monitoredMap.remove(key);
    }

    static {
        try {
            monitorService = FileSystems.getDefault().newWatchService();
            Thread thread = new Thread(new Runnable() {
                @Override
                @SuppressWarnings("unchecked")
                public void run() {
                    for (;;) {
                        try {
                            WatchKey key = monitorService.take();
                            ConfigManager config = getWatchConfig(key);
                            if (config != null) {
                                for (WatchEvent event : key.pollEvents()) {
                                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                                        continue;
                                    }
                                    Path eventPath = ((WatchEvent<Path>) event).context();
                                    Path configPath = config._file.toPath().getFileName();
                                    if (eventPath.compareTo(configPath) == 0) {
                                        if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                            System.out.println("Config file is deleted.");
                                            config.clearReload();
                                        } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                            System.out.println("Config file is modified.");
                                            config.reloadFile();
                                        }
                                    }
                                }
                            }
                            if (!key.reset())
                                removeWatchConfig(key);
                        } catch (InterruptedException e) {
                            break;
                        } catch (Exception ex) {
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isMonitorFileChange() {
        return monitorFileChange;
    }

    public void setMonitorFileChange(boolean monitorFileChange) throws IOException {
        if (!isLoadFromFile()) {
            monitorFileChange = false;
        }
        if (this.monitorFileChange == monitorFileChange)
            return;
        this.monitorFileChange = monitorFileChange;
        if (monitorFileChange) {
            try {
                watchKey = _file.toPath().getParent().register(monitorService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY,
                        StandardWatchEventKinds.ENTRY_DELETE);
                setWatchConfig(watchKey, this);
            } catch (Exception ex) {
                watchKey = null;
                throw ex;
            }
        } else {
            if (watchKey != null) {
                watchKey.cancel();
                removeWatchConfig(watchKey);
                watchKey = null;
            }
        }
    }

    public void addChangeListener(ChangeListener listener) {
        listenerSet.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listenerSet.remove(listener);
    }

    public boolean isLoadFromFile() {
        return _file != null;
    }

    public ConfigManager(String resource) throws IOException {
        loadResource(resource);
    }

    public ConfigManager(File file) {
        try {
            loadFile(file);
        } catch (IOException e) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.WARNING, "Load config file failed: " + file.toString());
        }
    }

    public ConfigManager(URL url) throws IOException {
        loadURL(url);
    }

    public ConfigManager() {
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return this.appName;
    }

    private void fireChanged() {
        for (ChangeListener listener: listenerSet) {
            try {
                listener.onConfigChanged(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void clear() {
        rawContent = null;
        rootObj = null;
    }

    private void clearReload() {
        clear();
        fireChanged();
    }

    private void reloadFile() {
        try {
            // If did not sleep a while then it maybe will load failed.
            Thread.sleep(100);
            loadFile(_file, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            setMonitorFileChange(true);
        } catch (Exception ex) {
            Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        fireChanged();
    }

    /**
     * Load config file either from data dir or from application resource dir,
     * This method let developer can load default setting from resource dir and
     * monitor data dir change, if new configuration file created in data dir
     * config manager will receive a notify.
     * @param dataDir Configurable directory path
     * @param configFileName Config file name if load from resource, then it be a resource file name
     * @throws IOException Not found file either in data dir or in resource.
     */
    public void load(String dataDir, String configFileName) throws IOException {
        loadDefaultResource(configFileName);
        rawContent = null;
        rootObj = null;
        if (dataDir == null) {
            merge();
            return;
        }
        dataDir = dataDir.replace('\\', '/');
        String path = (dataDir.endsWith("/") ? dataDir : dataDir + "/") + configFileName;
        File configFile = new File(path);
        try {
            loadFile(configFile, false);
        } catch (IOException ex) {
            System.err.println("Load configuration file error: " + configFile);
            ex.printStackTrace();
        }
    }

    public void loadDefaultResource(String resource) {
        try {
            defaultRoot = Serializer.fromJson(
                    Serializer.loadTextResource(resource), JsonElement.class);
        } catch (Exception ex) {
            System.out.println("No default resource found: " + resource);
            defaultRoot = null;
        }
    }

    public void loadResource(String resource) throws IOException {
        rawContent = null; // Should set to null because loadTextResource maybe throw exception
        rootObj = null;
        defaultRoot = null;
        rawContent = Serializer.loadTextResource(resource);
    }

    public void loadFile(File file) throws IOException {
        loadFile(file, true);
    }

    private void loadFile(File file, boolean clearDefault) throws IOException {
        rawContent = null;
        rootObj = null;
        if (clearDefault) {
            defaultRoot = null;
        }
        try {
            setMonitorFileChange(false);
            if (file.exists())
                rawContent = Serializer.loadTextFile(file);
            else
                System.out.println("Configuration file does not exist!");
        } finally {
            _file = file;
            merge();
        }
    }
    public void loadURL(URL url) throws IOException {
        loadURL(url, true);
    }
    public void loadURL(URL url, boolean clearDefault) throws IOException {
        rawContent = null;
        rootObj = null;
        if (clearDefault) {
            defaultRoot = null;
        }
        try {
            rawContent = Serializer.loadTextURL(url);
        } finally {
            merge();
        }
    }

    public String getRawContent() {
        return rawContent;
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
        } else
            newRoot = Serializer.fromJson(rawContent, JsonElement.class);
        if (defaultRoot == null) {
            rootObj = newRoot;
            return;
        } else if (newRoot == null) {
            rootObj = defaultRoot;
            return;
        } else {
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

    public List getList(String jsonPath) {
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



    /**
     * Get application data directory path, it's combined by Java system property name or
     * OS environment variable name(by default var name is 'APP_DATA_DIR') and plus application name.
     * @param environmentValueName Java system property name or OS environment variable name
     * @param appName Application name, if pass null then return the path of the 'APP_DATA_DIR'
     * @return App data dir path or null if env variable not exists.
     */
    public static String getAppDataDir(String environmentValueName, String appName) {
        if (environmentValueName == null)
            environmentValueName = "app.data.dir";
        String envStyle = environmentValueName.replace('.', '_').toUpperCase();
        String javaStyle = environmentValueName.replace('_', '.').toLowerCase();
        String dataDir = System.getProperty(javaStyle);
        if (dataDir == null) { // there have many nested blocks because for performance reason
            dataDir = System.getProperty(envStyle);
            if (dataDir == null) {
                dataDir = System.getenv(envStyle);
                if (dataDir == null) {
                    dataDir = System.getenv(javaStyle);
                    if (dataDir == null) {
                        String osName = System.getProperty("os.name");
                        if (osName.matches("(?i)windows.*")) {
                            dataDir = System.getenv("APPDATA");
                        } else {
                            dataDir = System.getProperty("user.home") + "/.webapp";
                        }
                    }
                }
            }
        }

        if (dataDir != null) {
            if (appName == null) {
                return dataDir;
            }
            while (appName.startsWith("/") || appName.startsWith("\\"))
                appName = appName.substring(1);
            if (dataDir.endsWith("/") || dataDir.endsWith("\\"))
                dataDir += appName;
            else
                dataDir += ("/" + appName);
            return dataDir;
        } else
            return null;
    }


    /**
     * Get application data directory path, it's combined by
     * system environment variable 'APP_DATA_DIR' or 'app.data.dir' and plus application name.
     * @param appName Application name
     * @return App data dir path or null if env variable not exists.
     */
    public static String getAppDataDir(String appName) {
        return getAppDataDir(null, appName);
    }

    /**
     * Get application data directory path, it's combined by
     * system environment variable 'APP_DATA_DIR' or 'app.data.dir'.
     * @return App data dir path or null if env variable not exists.
     */
    public static String getAppDataDir() {
        return getAppDataDir(null, null);
    }
}
