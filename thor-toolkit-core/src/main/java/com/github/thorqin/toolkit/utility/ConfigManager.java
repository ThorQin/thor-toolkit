package com.github.thorqin.toolkit.utility;

import com.google.gson.*;
import org.joda.time.DateTime;
import java.io.*;
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
    public static interface ChangeListener {
        void onChanged(ConfigManager config);
    }

    private static WatchService monitorService = null;
    private static final Map<WatchKey, ConfigManager> monitoredMap = new HashMap<>();

    private File _file = null;
    private boolean monitorFileChange = false;
    private Set<ChangeListener> listenerSet = new HashSet<>();
    private WatchKey watchKey = null;
    private JsonElement rootObj = null;
    private String rawContent = null;

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

    private void fireChanged() {
        for (ChangeListener listener: listenerSet) {
            try {
                listener.onChanged(this);
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
            loadFile(_file);
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
        if (dataDir == null) {
            loadResource(configFileName);
            return;
        }
        dataDir = dataDir.replace('\\', '/');
        String path = (dataDir.endsWith("/") ? dataDir : dataDir + "/") + configFileName;
        File configFile = new File(path);
        try {
            loadFile(configFile);
        } catch (IOException ex) {
            loadResource(configFileName);
        }
    }

    public void loadResource(String resource) throws IOException {
        rawContent = null;
        rootObj = null;
        rawContent = Serializer.loadTextResource(resource);
    }

    public void loadFile(File file) throws IOException {
        setMonitorFileChange(false);
        rawContent = null;
        rootObj = null;
        try {
            rawContent = Serializer.loadTextFile(file);
        } finally {
            _file = file;
        }
    }

    public void loadURL(URL url) throws IOException {
        rawContent = null;
        rootObj = null;
        rawContent = Serializer.loadTextURL(url);
    }

    public String getRawContent() {
        return rawContent;
    }

    /**
     * Get the value which is indicated by the json path.
     * @param jsonPath JSON path to indicate where we should extract info from the configuration.
     * @return JsonElement depends on which type of the JSON path pointed to.
     */
    public JsonElement get(String jsonPath) {
        if (rawContent == null)
            return null;
        if (rootObj == null)
            rootObj = Serializer.fromJson(rawContent, JsonElement.class);
        String[] paths = jsonPath.split("(?<!\\\\)/");
        JsonElement obj = rootObj;
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
        JsonElement obj = get(jsonPath);
        return obj != null ? obj.getAsString() : null;
    }
    public String getString(String jsonPath, String defaultValue) {
        try {
            String val = getString(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public Integer getInteger(String jsonPath) {
        JsonElement obj = get(jsonPath);
        return obj != null ? obj.getAsInt() : null;
    }
    public Integer getInteger(String jsonPath, Integer defaultValue) {
        try {
            Integer val = getInteger(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public Long getLong(String jsonPath) {
        JsonElement obj = get(jsonPath);
        return obj != null ? obj.getAsLong() : null;
    }
    public Long getLong(String jsonPath, Long defaultValue) {
        try {
            Long val = getLong(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public Short getShort(String jsonPath) {
        JsonElement obj = get(jsonPath);
        return obj != null ? obj.getAsShort() : null;
    }
    public Short getShort(String jsonPath, Short defaultValue) {
        try {
            Short val = getShort(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public Byte getByte(String jsonPath) {
        JsonElement obj = get(jsonPath);
        return obj != null ? obj.getAsByte() : null;
    }
    public Byte getByte(String jsonPath, Byte defaultValue) {
        try {
            Byte val = getByte(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public BigInteger getBigInteger(String jsonPath) {
        JsonElement obj = get(jsonPath);
        return obj != null ? obj.getAsBigInteger() : null;
    }
    public BigInteger getBigInteger(String jsonPath, BigInteger defaultValue) {
        try {
            BigInteger val = getBigInteger(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public BigDecimal getBigDecimal(String jsonPath) {
        JsonElement obj = get(jsonPath);
        return obj != null ? obj.getAsBigDecimal() : null;
    }
    public BigDecimal getBigDecimal(String jsonPath, BigDecimal defaultValue) {
        try {
            BigDecimal val = getBigDecimal(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public Boolean getBoolean(String jsonPath) {
        JsonElement obj = get(jsonPath);
        return obj != null ? obj.getAsBoolean() : null;
    }
    public Boolean getBoolean(String jsonPath, Boolean defaultValue) {
        try {
            Boolean val = getBoolean(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public Float getFloat(String jsonPath) {
        JsonElement obj = get(jsonPath);
        return obj != null ? obj.getAsFloat() : null;
    }
    public Float getFloat(String jsonPath, Float defaultValue) {
        try {
            Float val = getFloat(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public Double getDouble(String jsonPath) {
        JsonElement obj = get(jsonPath);
        return obj != null ? obj.getAsDouble() : null;
    }
    public Double getDouble(String jsonPath, Double defaultValue) {
        try {
            Double val = getDouble(jsonPath);
            return (val != null ? val : defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public DateTime getDateTime(String jsonPath) {
        JsonElement obj = get(jsonPath);
        if (obj != null)
            return StringUtils.parseISO8601(obj.getAsString());
        else
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


    /**
     * Get application data directory path, it's combined by
     * system environment variable 'APP_DATA_DIR' and plus application name.
     * @param appName Application name
     * @return App data dir path or null if env variable not exists.
     */
    public static String getAppDataDir(String appName) {
        String dataDir = System.getenv().get("APP_DATA_DIR");
        if (dataDir != null) {
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
}
