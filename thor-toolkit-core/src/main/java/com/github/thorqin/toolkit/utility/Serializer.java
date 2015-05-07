package com.github.thorqin.toolkit.utility;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.FileLock;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public final class Serializer {
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
	
	public interface StringConvertor {
		void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException;
	}
	
	private static final Map<Class<?>, StringConvertor> convertMapping;
	
	static {
		convertMapping = new HashMap<>();
		convertMapping.put(String.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.set(obj, value);
			}
		});
		convertMapping.put(Byte.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				if (value == null)
                    field.set(obj, null);
                else
                    field.set(obj, Byte.valueOf(value));
			}
		});
		convertMapping.put(Short.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
                if (value == null)
                    field.set(obj, null);
                else
                    field.set(obj, Short.valueOf(value));
			}
		});
		convertMapping.put(Integer.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
                if (value == null)
                    field.set(obj, null);
                else
                    field.set(obj, Integer.valueOf(value));
			}
		});
		convertMapping.put(Long.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
                if (value == null)
                    field.set(obj, null);
                else
                    field.set(obj, Long.valueOf(value));
			}
		});
		convertMapping.put(Float.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
                if (value == null)
                    field.set(obj, null);
                else
                    field.set(obj, Float.valueOf(value));
			}
		});
		convertMapping.put(Double.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
                if (value == null)
                    field.set(obj, null);
                else
                    field.set(obj, Double.valueOf(value));
			}
		});
		convertMapping.put(Boolean.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
                if (value == null)
                    field.set(obj, null);
                else
                    field.set(obj, Boolean.valueOf(value));
			}
		});
		
		convertMapping.put(byte.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.setByte(obj, Byte.parseByte(value));
			}
		});
		convertMapping.put(short.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.setShort(obj, Short.parseShort(value));
			}
		});
		convertMapping.put(int.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.setInt(obj, Integer.parseInt(value));
			}
		});
		convertMapping.put(long.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.setLong(obj, Long.parseLong(value));
			}
		});
		convertMapping.put(float.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.setFloat(obj, Float.parseFloat(value));
			}
		});
		convertMapping.put(double.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.setDouble(obj, Double.parseDouble(value));
			}
		});
		convertMapping.put(boolean.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.setBoolean(obj, Boolean.parseBoolean(value));
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

	public static StringConvertor getStringConvertor(Class<?> type) {
		return convertMapping.get(type);
	}
	
	private static Kryo getKryo() {
		Kryo kryo = localKryo.get();
		if (kryo == null) {
			kryo = new Kryo();
			localKryo.set(kryo);
		}
		return kryo;
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
	
	public static <T> void saveJsonFile(T obj, String filename) throws IOException {
		saveJsonFile(obj, filename, false);
	}
	public static <T> void saveJsonFile(T obj, File file) throws IOException {
		saveJsonFile(obj, file, false);
	}	
	public static <T> void saveJsonFile(T obj, String filename, boolean prettyPrint) throws IOException {
		try (OutputStream stream = new FileOutputStream(filename); 
				OutputStreamWriter writer = new OutputStreamWriter(stream, "utf-8")) {
			if (prettyPrint)
				gsonPrettyPrinting.toJson(obj, writer);
			else
				gson.toJson(obj, writer);
		}
	}
	public static <T> void saveJsonFile(T obj, File file, boolean prettyPrint) throws IOException {
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
			T obj = gson.fromJson(reader, type);
			return obj;
		}
	}
	public static <T> T fromJson(byte[] bytes, Type type) throws IOException, ClassCastException {
		try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
				InputStreamReader reader = new InputStreamReader(byteStream)) {
			T obj = gson.fromJson(reader, type);
			return obj;
		}
	}
	public static <T> T fromJson(String json) throws ClassCastException {
		Type typeOfT = new TypeToken<T>(){}.getType();
		T obj = gson.fromJson(json, typeOfT);
		return obj;
	}
	public static <T> T fromJson(String json, Class<T> type) throws ClassCastException {
		T obj = gson.fromJson(json, type);
		return obj;
	}
	
	public static <T> T fromJson(String json, Type type) throws ClassCastException {
		T obj = gson.fromJson(json, type);
		return obj;
	}
	public static <T> T fromJson(Reader reader, Class<T> type) throws IOException, ClassCastException {
		T obj = gson.fromJson(reader, type);
		return obj;
	}
	public static <T> T fromJson(Reader reader, Type type) throws IOException, ClassCastException {
		T obj = gson.fromJson(reader, type);
		return obj;
	}
	public static <T> T fromJson(JsonElement jsonElement, Class<T> type) throws ClassCastException {
		T obj = gson.fromJson(jsonElement, type);
		return obj;
	}

	public static <T> T fromJson(JsonElement jsonElement, Type type) throws ClassCastException {
		T obj = gson.fromJson(jsonElement, type);
		return obj;
	}
	public static <T> T loadJsonResource(String resource, Class<T> type) throws IOException, ClassCastException {
		try (InputStream in = Serializer.class.getClassLoader().getResourceAsStream(resource);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {
			return fromJson(reader, type);
		}
	}
	public static <T> T loadJsonResource(String resource, Type type) throws IOException, ClassCastException {
		try (InputStream in = Serializer.class.getClassLoader().getResourceAsStream(resource);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {
			return fromJson(reader, type);
		}
	}
	
	public static <T> T loadJsonFile(String filename, Class<T> type) throws IOException, ClassCastException {
		try (InputStream in = new FileInputStream(filename);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {
			return fromJson(reader, type);
		}
	}
	
	public static <T> T loadJsonFile(String filename, Type type) throws IOException, ClassCastException {
		try (InputStream in = new FileInputStream(filename);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {
			return fromJson(reader, type);
		}
	}
	
	public static <T> T loadJsonFile(File file, Type type) throws IOException, ClassCastException {
		try (InputStream in = new FileInputStream(file);
				InputStreamReader reader = new InputStreamReader(in, "utf-8")) {
			return fromJson(reader, type);
		}
	}
	
	// WWW FORM URL ENCODING ...

	public static <T> String toUrlEncoding(T obj) throws IllegalAccessException, UnsupportedEncodingException {
		if (obj == null)
			return "";
		StringBuilder sb = new StringBuilder();
		Class<?> type = obj.getClass();
		for (Field field: type.getDeclaredFields()) {
			if (field.isAccessible()) {
				if (sb.length() > 0)
					sb.append("&");
				sb.append(field.getName());
				sb.append("=");
				sb.append(URLEncoder.encode(field.get(obj).toString(), "utf-8"));
			}
		}
		return sb.toString();
	}

    public static String toUrlEncoding(Map<String, String> obj) throws IllegalAccessException, UnsupportedEncodingException {
        if (obj == null)
            return "";
        StringBuilder sb = new StringBuilder();
        Class<?> type = obj.getClass();
        for (String key: obj.keySet()) {
            if (sb.length() > 0)
                sb.append("&");
            sb.append(URLEncoder.encode(key, "utf-8"));
            sb.append("=");
            sb.append(URLEncoder.encode(obj.get(key), "utf-8"));
        }
        return sb.toString();
    }

    public static Map<String, String> fromUrlEncoding(String formData) throws UnsupportedEncodingException {
        if (formData == null)
            return null;
        String[] parts = formData.split("&");
        Map<String, String> map = new HashMap<>();
        for (String part : parts) {
            String[] pair = part.split("=");
            if (pair.length != 2)
                continue;
            String key = URLDecoder.decode(pair[0], "utf-8");
            String val = URLDecoder.decode(pair[1], "utf-8");
            map.put(key, val);
        }
        return map;
    }

	public static <T> T fromUrlEncoding(String formData, Class<T> type) throws InstantiationException, IllegalAccessException, UnsupportedEncodingException {
        Map<String, String> map = fromUrlEncoding(formData);
        if (map == null)
            return null;
        T obj = type.newInstance();
		for (Field field: type.getDeclaredFields()) {
			String key;
			SerializedName name = field.getAnnotation(SerializedName.class);
			if (name == null)
				key = field.getName();
			else
				key = name.value();
			String val = map.get(key);
			if (val == null)
				continue;
			Class<?> fieldType = field.getType();
			StringConvertor convertor = convertMapping.get(fieldType);
			if (convertor != null) {
				field.setAccessible(true);
				convertor.setValue(obj, field, val);
			}
		}
		return obj;
	}

	public static String loadTextResource(String resourceName) throws IOException {
		try (InputStream in = Serializer.class.getClassLoader().getResourceAsStream(resourceName)) {
            return loadTextStream(in);
		}
	}

	public static String loadTextFile(File file) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			return loadTextStream(in);
		}
	}

	public static String loadTextURL(URL url) throws IOException {
		URLConnection conn = url.openConnection();
		conn.setUseCaches(false);
		try (InputStream in = conn.getInputStream()) {
			return loadTextStream(in);
		}
	}

	public static byte[] readStreamBytes(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int readSize;
		while ((readSize = in.read(buffer)) > 0) {
			out.write(buffer, 0, readSize);
		}
		return out.toByteArray();
	}

	public static String loadTextStream(InputStream in) throws IOException {
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

    public static void saveTextFile(String content, String filename, String encoding) throws IOException {
        try (OutputStream stream = new FileOutputStream(filename);
             OutputStreamWriter writer = new OutputStreamWriter(stream, encoding)) {
            writer.write(content);
        }
    }
    public static void saveTextFile(String content, File file, String encoding) throws IOException {
        try (OutputStream stream = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(stream, encoding)) {
            writer.write(content);
        }
    }
    public static void saveTextFile(String content, String filename) throws IOException {
        saveTextFile(content, filename, "utf-8");
    }
    public static void saveTextFile(String content, File file) throws IOException {
        saveTextFile(content, file, "utf-8");
    }
}
