package com.github.thorqin.toolkit.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thor on 8/19/16.
 */
public abstract class JsonAccessor {
    protected JsonElement rootObj = null;

    public static JsonElement getFromRoot(String jsonPath, JsonElement root) {
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
}
