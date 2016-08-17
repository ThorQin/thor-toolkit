package com.github.thorqin.toolkit.utility;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.google.common.base.Strings;
import com.google.common.math.BigIntegerMath;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.FormatUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.*;


public final class Serializer {

    public final static Type COMMON_MAP = new TypeToken<Map<String, Object>>(){}.getType();
    public final static Type COMMON_LIST = new TypeToken<List<Object>>(){}.getType();

    public static <T> Type makeListType(Class<T> type) {
        return new TypeToken<List<T>>(){}.getType();
    }

    public static <K, V> Type makeMapType(Class<K> keyType, Class<V> valueType) {
        return new TypeToken<Map<K, V>>(){}.getType();
    }

	public static class DateTimeAdapter extends TypeAdapter<DateTime>
			implements InstanceCreator<DateTime>,
			JsonSerializer<DateTime>, JsonDeserializer<DateTime> {

		@Override
		public void write(JsonWriter out, DateTime value) throws IOException {
            if (value == null)
                out.nullValue();
            else
			    out.value(StringUtils.toISO8601(value));
		}

		@Override
		public DateTime read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            if (token != null) {
                if (token.name().equals("NULL")) {
                    in.nextNull();
                    return null;
                }
			    return StringUtils.parseISO8601(in.nextString());
            } else
                return null;
		}

		@Override
		public DateTime createInstance(Type type) {
			return new DateTime();
		}

		@Override
		public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
			return context.serialize(src);
		}

		@Override
		public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return context.deserialize(json, typeOfT);
		}
	}

	public static class DateAdapter extends TypeAdapter<Date>
			implements InstanceCreator<Date>,
			JsonSerializer<Date>, JsonDeserializer<Date> {

		@Override
		public void write(JsonWriter out, Date value) throws IOException {
            if (value == null)
                out.nullValue();
            else
			    out.value(StringUtils.toISO8601(new DateTime(value.getTime(), DateTimeZone.getDefault())));
		}

		@Override
		public Date read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            if (token != null) {
                if (token.name().equals("NULL")) {
                    in.nextNull();
                    return null;
                }
                return StringUtils.parseISO8601(in.nextString()).toDate();
            } else
                return null;
		}

		@Override
		public Date createInstance(Type type) {
			return new Date();
		}

		@Override
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			return context.serialize(src);
		}

		@Override
		public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return context.deserialize(json, typeOfT);
		}
	}

	private static final ThreadLocal<Kryo> localKryo = 
			new ThreadLocal<>();
	private static final Gson gson = new GsonBuilder()
			.registerTypeAdapter(Date.class, new DateAdapter())
			.registerTypeAdapter(DateTime.class, new DateTimeAdapter())
			.create();
	private static final Gson gsonPrettyPrinting = new GsonBuilder()
			.registerTypeAdapter(Date.class, new DateAdapter())
			.registerTypeAdapter(DateTime.class, new DateTimeAdapter())
			.setPrettyPrinting()
			.create();
    private static final ThreadLocal<Yaml> localYaml =
            new ThreadLocal<>();
	
	public interface StringConverter {
		Object parse(String value) throws IllegalArgumentException;
	}

	public static void setFieldByString(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
		Class<?> type = field.getType();
		StringConverter converter = convertMapping.get(type);
		if (converter != null) {
			Object val = converter.parse(value);
			field.setAccessible(true);
			field.set(obj, val);
		}
	}

    public static void fillObjectByMap(Object obj, Map<String, Object> map) throws IllegalAccessException {
        if (obj == null || map == null)
            return;
        Class<?> type = obj.getClass();
        Set<Field> fields = getVisibleFields(type);
        for (Field field: fields) {
            String key = field.getName();
            Object value = map.get(key);
            if (value != null) {
                field.setAccessible(true);
                field.set(obj, value);
            }
        }
    }

	@SuppressWarnings("unchecked")
	public static <T> T parseString(String value, Class<T> type) {
		StringConverter converter = convertMapping.get(type);
		return (T) converter.parse(value);
	}

	
	private static final Map<Class<?>, StringConverter> convertMapping;
	
	static {
		convertMapping = new HashMap<>();
		convertMapping.put(String.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return value;
			}
		});
		convertMapping.put(Byte.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? null : Byte.valueOf(value);
			}
		});
		convertMapping.put(Short.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? null : Short.valueOf(value);
			}
		});
		convertMapping.put(Integer.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? null : Integer.valueOf(value);
			}
		});
		convertMapping.put(Long.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? null : Long.valueOf(value);
			}
		});
		convertMapping.put(Float.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? null : Float.valueOf(value);
			}
		});
		convertMapping.put(Double.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? null : Double.valueOf(value);
			}
		});
		convertMapping.put(Boolean.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? null : Boolean.valueOf(value);
			}
		});
		
		convertMapping.put(byte.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? 0 : Byte.parseByte(value);
			}
		});
		convertMapping.put(short.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? 0 : Short.parseShort(value);
			}
		});
		convertMapping.put(int.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? 0 : Integer.parseInt(value);
			}
		});
		convertMapping.put(long.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? 0 : Long.parseLong(value);
			}
		});
		convertMapping.put(float.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? 0 : Float.parseFloat(value);
			}
		});
		convertMapping.put(double.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? 0 : Double.parseDouble(value);
			}
		});
		convertMapping.put(boolean.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? false : Boolean.parseBoolean(value);
			}
		});
		convertMapping.put(DateTime.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? null : DateTime.parse(value);
			}
		});
		convertMapping.put(BigDecimal.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? null : new BigDecimal(value);
			}
		});
		convertMapping.put(BigInteger.class, new StringConverter() {
			@Override
			public Object parse(String value) throws IllegalArgumentException {
				return Strings.isNullOrEmpty(value) ? null : new BigInteger(value);
			}
		});
	}
	
	public static <T> String getTypeName(T obj) {
		if (obj == null)
			return Void.class.getName();
		else {
			return obj.getClass().getName();
		}
	}

	private static Kryo getKryo() {
		Kryo kryo = localKryo.get();
		if (kryo == null) {
			kryo = new Kryo();
			localKryo.set(kryo);
		}
		return kryo;
	}

    private static Yaml getYaml() {
        Yaml yaml = localYaml.get();
        if (yaml == null) {
            yaml = new Yaml();
            localYaml.set(yaml);
        }
        return yaml;
    }

	public static <T> byte[] toKryo(T obj) {
		Kryo kryo = getKryo();
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try (Output output = new Output(byteStream)) {
			kryo.writeClassAndObject(output, obj);
		}
		return byteStream.toByteArray();
	}
	@SuppressWarnings("unchecked")
	public static <T> T fromKryo(byte[] bytes) throws ClassCastException {
		Kryo kryo = getKryo();
		T obj;
		try (Input input = new Input(new ByteArrayInputStream(bytes))) {
			obj = (T)kryo.readClassAndObject(input);
		}
		return obj;
	}

	public static <T> T clone(T obj) {
		Kryo kryo = getKryo();
		return kryo.copy(obj);
	}
	
	/**
	 * Deep copy
	 * @param <T> Type name
	 * @param obj Src object
	 * @return Deep copied object
	 */
	public static <T> T copy(T obj) {
		Kryo kryo = getKryo();
		return kryo.copy(obj);
	}
	
	/**
	 * Make a shallow copy of the object
	 * @param <T> Type name
	 * @param obj Src object
	 * @return Shallow copied object
	 */
	public static <T> T shallowCopy(T obj) {
		Kryo kryo = getKryo();
		return kryo.copyShallow(obj);
	}
	
	
	// JSON ENCODING ...
	
	public static <T> byte[] toJsonBytes(T obj) throws IOException {
		try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
				OutputStreamWriter writer = new OutputStreamWriter(byteStream)) {
			gson.toJson(obj, writer);
			return byteStream.toByteArray();
		}
	}
	
	public static <T> void writeJsonFile(T obj, String filename) throws IOException {
		writeJsonFile(obj, filename, false);
	}
	public static <T> void writeJsonFile(T obj, File file) throws IOException {
		writeJsonFile(obj, file, false);
	}	
	public static <T> void writeJsonFile(T obj, String filename, boolean prettyPrint) throws IOException {
		try (OutputStream stream = new FileOutputStream(filename); 
				OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8")) {
			if (prettyPrint)
				gsonPrettyPrinting.toJson(obj, writer);
			else
				gson.toJson(obj, writer);
		}
	}
	public static <T> void writeJsonFile(T obj, File file, boolean prettyPrint) throws IOException {
		try (OutputStream stream = new FileOutputStream(file); 
				OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8")) {
			if (prettyPrint)
				gsonPrettyPrinting.toJson(obj, writer);
			else
				gson.toJson(obj, writer);
		}
	}
	public static <T> String toJsonString(T obj) {
		return toJsonString(obj, false);
	}
	public static <T> String toJsonString(T obj, boolean prettyPrint) {
		if (prettyPrint)
			return gsonPrettyPrinting.toJson(obj);
		else
			return gson.toJson(obj);
	}
	
	public static <T> void toJson(T obj, Appendable writer) throws IOException {
		Type typeOfT = new TypeToken<T>(){}.getType();
		gson.toJson(obj, typeOfT, writer);
	}

	public static <T> JsonElement toJsonElement(T obj) {
		return gson.toJsonTree(obj);
	}
	
	public static <T> T fromJson(byte[] bytes) throws IOException, ClassCastException {
		try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
				InputStreamReader reader = new InputStreamReader(byteStream)) {
			Type typeOfT = new TypeToken<T>(){}.getType();
			T obj = gson.fromJson(reader, typeOfT);
			return obj;
		}
	}
	public static <T> T fromJson(byte[] bytes, Class<T> type) throws IOException, ClassCastException {
		try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
				InputStreamReader reader = new InputStreamReader(byteStream)) {
            return gson.fromJson(reader, type);
		}
	}
	public static <T> T fromJson(byte[] bytes, Type type) throws IOException, ClassCastException {
		try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
				InputStreamReader reader = new InputStreamReader(byteStream)) {
            return gson.fromJson(reader, type);
		}
	}
	public static <T> T fromJson(String json) throws ClassCastException {
		Type typeOfT = new TypeToken<T>(){}.getType();
        return gson.fromJson(json, typeOfT);
	}
	public static <T> T fromJson(String json, Class<T> type) throws ClassCastException {
        return gson.fromJson(json, type);
	}
	
	public static <T> T fromJson(String json, Type type) throws ClassCastException {
        return gson.fromJson(json, type);
	}
	public static <T> T fromJson(Reader reader, Class<T> type) throws IOException, ClassCastException {
        return gson.fromJson(reader, type);
	}
    public static <T> T fromJson(Reader reader) throws IOException, ClassCastException {
        Type typeOfT = new TypeToken<T>(){}.getType();
        return gson.fromJson(reader, typeOfT);
    }
	public static <T> T fromJson(Reader reader, Type type) throws IOException, ClassCastException {
        return gson.fromJson(reader, type);
	}
    public static <T> T fromJson(JsonElement jsonElement) throws ClassCastException {
        Type typeOfT = new TypeToken<T>(){}.getType();
        return gson.fromJson(jsonElement, typeOfT);
    }
	public static <T> T fromJson(JsonElement jsonElement, Class<T> type) throws ClassCastException {
        return gson.fromJson(jsonElement, type);
	}

	public static <T> T fromJson(JsonElement jsonElement, Type type) throws ClassCastException {
        return gson.fromJson(jsonElement, type);
	}
	public static <T> T readJsonResource(String resource, Class<T> type) throws IOException, ClassCastException {
		try (InputStream in = Serializer.class.getClassLoader().getResourceAsStream(resource);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {
			return fromJson(reader, type);
		}
	}
	public static <T> T readJsonResource(String resource, Type type) throws IOException, ClassCastException {
		try (InputStream in = Serializer.class.getClassLoader().getResourceAsStream(resource);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {
			return fromJson(reader, type);
		}
	}
    public static <T> T readJsonResource(String resource) throws IOException, ClassCastException {
        Type typeOfT = new TypeToken<T>(){}.getType();
        return readJsonResource(resource, typeOfT);
    }
	
	public static <T> T readJsonFile(String filename, Class<T> type) throws IOException, ClassCastException {
		try (InputStream in = new FileInputStream(filename);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {
			return fromJson(reader, type);
		}
	}

    public static <T> T readJsonFile(String filename) throws IOException, ClassCastException {
        Type typeOfT = new TypeToken<T>(){}.getType();
        return readJsonFile(filename, typeOfT);
    }
	
	public static <T> T readJsonFile(String filename, Type type) throws IOException, ClassCastException {
		try (InputStream in = new FileInputStream(filename);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {
			return fromJson(reader, type);
		}
	}

	public static <T> T readJsonFile(File file, Type type) throws IOException, ClassCastException {
		try (InputStream in = new FileInputStream(file);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {
			return fromJson(reader, type);
		}
	}

    public static Object fromYaml(String yaml) {
        Yaml yamlParser = getYaml();
        Object obj = yamlParser.load(yaml);
        return obj;
    }

    public static <T> T fromYaml(String yaml, Class<T> type) {
        Yaml yamlParser = getYaml();
        Object obj = yamlParser.load(yaml);
        return fromJson(Serializer.toJsonElement(obj), type);
    }
	
	// WWW FORM URL ENCODING ...

	public static <T> String toUrlEncoding(T obj) throws IllegalAccessException {
		if (obj == null)
			return "";
		StringBuilder sb = new StringBuilder();
		Class<?> type = obj.getClass();
        try {
            for (Field field : getVisibleFields(type)) {
                if (field.isAccessible()) {
                    if (sb.length() > 0)
                        sb.append("&");
                    sb.append(URLEncoder.encode(field.getName(), "utf-8"));
                    sb.append("=");
                    Object value = field.get(obj);
                    if (value != null)
                        sb.append(URLEncoder.encode(value.toString(), "utf-8"));
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
		return sb.toString();
	}

    public static String toUrlEncoding(Map<String, String> obj) throws IllegalAccessException {
        if (obj == null)
            return "";
        StringBuilder sb = new StringBuilder();
        try {
            for (String key : obj.keySet()) {
                if (key == null)
                    continue;
                if (sb.length() > 0)
                    sb.append("&");
                sb.append(URLEncoder.encode(key, "utf-8"));
                sb.append("=");
                String value = obj.get(key);
                if (value != null)
                    sb.append(URLEncoder.encode(value, "utf-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
        return sb.toString();
    }

    public static Map<String, String> fromUrlEncoding(String formData) {
        if (formData == null)
            return null;
        String[] parts = formData.split("&");
        Map<String, String> map = new HashMap<>();
        try {
            for (String part : parts) {
                String[] pair = part.split("=");
                if (pair.length == 1) {
                    map.put(URLDecoder.decode(pair[0], "utf-8"), null);
                    continue;
                } else if (pair.length == 2) {
                    String key = URLDecoder.decode(pair[0], "utf-8");
                    String val = URLDecoder.decode(pair[1], "utf-8");
                    map.put(key, val);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        return map;
    }

	@SuppressWarnings("unchecked")
	public static <T> T fromUrlEncoding(String formData, Class<T> type) throws InstantiationException, IllegalAccessException {
        Map<String, String> map = fromUrlEncoding(formData);
        if (map == null)
            return null;
        if (Map.class.isAssignableFrom(type)) {
			return (T) map;
		}

        T obj = type.newInstance();
		for (Field field: getVisibleFields(type)) {
			String key;
			SerializedName name = field.getAnnotation(SerializedName.class);
			if (name == null)
				key = field.getName();
			else
				key = name.value();
			String val = map.get(key);
			if (val == null)
				continue;
			setFieldByString(obj, field, val);
		}
		return obj;
	}

    /**
     * Load text resource file and auto detect file charset encoding,
     * but any file without BOM will be regarded as UTF-8 encoding.
     * @param resourceName Resource to load
     * @return Text content
     * @throws IOException Throw when read file failed.
     */
	public static String readTextResource(String resourceName) throws IOException {
		String charset;
		try (InputStream in = Serializer.class.getClassLoader().getResourceAsStream(resourceName)) {
			charset = detectCharset(in);
		}
		try (InputStream in = Serializer.class.getClassLoader().getResourceAsStream(resourceName)) {
			return readTextStream(in, charset);
		}
	}

	public static String readTextResource(String resourceName, String charset) throws IOException {
		try (InputStream in = Serializer.class.getClassLoader().getResourceAsStream(resourceName)) {
			return readTextStream(in, charset);
		}
	}

	/**
	 * Load text data and auto detect charset encoding, if data contained BOM sign then remove it when return,
	 * default charset use JVM file.encoding property
	 * @param data Binary data to read
	 * @return Text content
	 * @throws IOException Throw when read data failed.
	 */
	public static String readTextData(final byte[] data) throws IOException {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data)) {
			return readTextStream(byteArrayInputStream);
		}
	}

	/**
     * Load text file in specifed charset encoding.
     * @param file File to load
     * @param charset Specifed charset
     * @return Text content
     * @throws IOException Throw when read file failed.
     */
    public static String readTextFile(File file, String charset) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            return readTextStream(in, charset);
        }
    }

    /**
     * Load text file and auto detect file charset encoding,
     * default charset use JVM file.encoding property
     * @param file File to load
     * @return Text content
     * @throws IOException Throw when read file failed.
     */
    public static String readTextFile(File file) throws IOException {
        String charset = detectCharset(file, null);
        try (FileInputStream in = new FileInputStream(file)) {
            return readTextStream(in, charset);
        }
    }

	public static String readTextURL(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		conn.setUseCaches(false);
		try (InputStream in = conn.getInputStream()) {
			return readTextStream(in);
		}
	}

	public static byte[] readFile(File file) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			return readStream(in);
		}
	}

	public static byte[] readResource(String resourceName) throws IOException {
		try (InputStream in = Serializer.class.getClassLoader().getResourceAsStream(resourceName)) {
			return readStream(in);
		}
	}

	public static byte[] readStream(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int readSize;
		while ((readSize = in.read(buffer)) > 0) {
			out.write(buffer, 0, readSize);
		}
		return out.toByteArray();
	}

    public static String detectCharset(File file) throws IOException {
        return detectCharset(file, null);
    }

	public static String detectCharset(File file, String defaultCharset) throws IOException {
		try (FileInputStream fileInputStream = new FileInputStream(file)) {
			return detectCharset(fileInputStream, defaultCharset);
		}
	}

	public static String detectCharset(InputStream inputStream) throws IOException {
		return detectCharset(inputStream, null);
	}

	/**
	 * Detect charset from InputStream, this method will change input stream position.
	 * @param inputStream Input stream which to be detected.
	 * @param defaultCharset Default charset if can not detected from stream.
	 * @return Text charset
	 * @throws IOException Throw when read file failed.
	 */
    public static String detectCharset(InputStream inputStream, String defaultCharset) throws IOException {
		PushbackInputStream pIn = new PushbackInputStream(inputStream, 3);
		byte[] bom = new byte[3];
		pIn.read(bom);
		String charset;
		if (bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
			charset = "utf-8";
		} else if (bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF) {
			charset = "utf-16be";
			pIn.unread(bom[2]);
		} else if (bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE) {
			charset = "utf-16le";
			pIn.unread(bom[2]);
		} else {
			// Do not have BOM, so, determine whether it is en UTF-8 charset.
			pIn.unread(bom);
			boolean utf8 = true;
			boolean ansi = true;
			byte[] buffer = new byte[1024];
			int size;
			int checkBytes = 0;
			READ_FILE:
			while ((size = pIn.read(buffer)) > 0) {
				for (int i = 0; i < size; i++) {
					if (checkBytes > 0) {
						if ((buffer[i] & 0xC0) == 0x80)
							checkBytes--;
						else {
							utf8 = false;
							ansi = false;
							break READ_FILE;
						}
					} else {
						if ((buffer[i] & 0x0FF) < 128)
							continue;
						ansi = false;
						if ((buffer[i] & 0xE0) == 0xC0)
							checkBytes = 1;
						else if ((buffer[i] & 0xF0) == 0xE0)
							checkBytes = 2;
						else {
							utf8 = false;
							break READ_FILE;
						}
					}
				}
			}
			if (ansi)
				charset = "us-ascii";
			else if (utf8)
				charset = "utf-8";
			else if (defaultCharset != null)
				charset = defaultCharset;
			else {
				charset = System.getProperty("file.encoding");
				if (charset == null)
					charset = "utf-8";
			}
		}
		return charset.trim().toLowerCase();
    }

	public static String readTextStream(InputStream in) throws IOException {
		PushbackInputStream pIn = new PushbackInputStream(in, 3);
		byte[] bom = new byte[3];
		pIn.read(bom);
		String encoding;
		if (bom[0] == (byte)0xEF && bom[1] == (byte)0xBB && bom[2] == (byte)0xBF) {
			encoding = "utf-8";
		} else if (bom[0] == (byte)0xFE && bom[1] == (byte)0xFF) {
			encoding = "utf-16";
			pIn.unread(bom[2]);
		} else if (bom[0] == (byte)0xFF && bom[1] == (byte)0xFE) {
			encoding = "utf-16le";
			pIn.unread(bom[2]);
		} else {
			encoding = "utf-8";
			pIn.unread(bom);
		}
		InputStreamReader reader = new InputStreamReader(pIn, encoding);
		StringBuilder sb = new StringBuilder();
		char[] buffer = new char[1024];
		int size;
		while ((size = reader.read(buffer)) > 0)
			sb.append(buffer, 0, size);
		return sb.toString();
	}

    public static String readTextStream(InputStream in, String charset) throws IOException {
        InputStreamReader reader = new InputStreamReader(in, charset);
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int size;
        while ((size = reader.read(buffer)) > 0)
            sb.append(buffer, 0, size);
        return sb.toString();
    }

    public static void writeTextFile(String filename, String content, String encoding) throws IOException {
        try (OutputStream stream = new FileOutputStream(filename);
             OutputStreamWriter writer = new OutputStreamWriter(stream, encoding)) {
            writer.write(content);
        }
    }
    public static void writeTextFile(File file, String content, String encoding) throws IOException {
        try (OutputStream stream = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(stream, encoding)) {
            writer.write(content);
        }
    }
    public static void writeTextFile(String filename, String content) throws IOException {
		writeTextFile(filename, content, "utf-8");
    }
    public static void writeTextFile(File file, String content) throws IOException {
		writeTextFile(file, content, "utf-8");
    }

	public static void writeFile(String filename, byte[] content) throws IOException {
		writeFile(new File(filename), content);
	}
	public static void writeFile(File file, byte[] content) throws IOException {
		try (OutputStream stream = new FileOutputStream(file)) {
			 stream.write(content);
		}
	}

    /**
     * If object1's content equals object2's content then return true
     * @param obj1 Object 1
     * @param obj2 Object 2
     * @return Whether objects content are equal.
     */
    public static boolean equals(Object obj1, Object obj2) {
        return toJsonString(obj1).equals(toJsonString(obj2));
    }

    private static void getAccessibleFields(Class<?> type, Set<Field> fieldSet) {
        if (type == null || type.equals(Object.class) ||
                type.isPrimitive() || type.equals(Void.class) ||
                type.equals(void.class))
            return;
        getAccessibleFields(type.getSuperclass(), fieldSet);
        for (Field field : type.getDeclaredFields()) {
            int modifier = field.getModifiers();
            if (Modifier.isStatic(modifier) || Modifier.isNative(modifier))
                continue;
            if (Modifier.isPublic(modifier) || Modifier.isProtected(modifier))
                fieldSet.add(field);
        }
    }

    /**
     * Get all visible fields of the specified class which are the sum of
     * getFields() and getDeclaredFields() returned result.
     * @param type Which type will extract fields info of
     * @return A set of fields
     */
    public static Set<Field> getVisibleFields(Class<?> type) {
        Set<Field> fieldSet = new HashSet<>();
        for (Field field : type.getDeclaredFields()) {
            fieldSet.add(field);
        }
        getAccessibleFields(type.getSuperclass(), fieldSet);
        return fieldSet;
    }


}
