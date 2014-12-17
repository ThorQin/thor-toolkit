package com.github.thorqin.toolkit.utility;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;


public class Serializer {
	private static final ThreadLocal<Kryo> localKryo = 
			new ThreadLocal<>();
	private static final Gson gson = new GsonBuilder()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
			.create();
	private static final Gson gsonPrettyPrinting = new GsonBuilder()
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
			.setPrettyPrinting()
			.create();
	
	private static interface StringConvertor {
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
				field.set(obj, Byte.valueOf(value));
			}
		});
		convertMapping.put(Short.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.set(obj, Short.valueOf(value));
			}
		});
		convertMapping.put(Integer.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.set(obj, Integer.valueOf(value));
			}
		});
		convertMapping.put(Long.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.set(obj, Long.valueOf(value));
			}
		});
		convertMapping.put(Float.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.set(obj, Float.valueOf(value));
			}
		});
		convertMapping.put(Double.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
				field.set(obj, Double.valueOf(value));
			}
		});
		convertMapping.put(Boolean.class, new StringConvertor() {
			@Override
			public void setValue(Object obj, Field field, String value) throws IllegalArgumentException, IllegalAccessException {
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
	public static <T> String toJsonString(T obj) throws IOException {
		return toJsonString(obj, false);
	}
	public static <T> String toJsonString(T obj, boolean prettyPrint) throws IOException {
		if (prettyPrint)
			return gsonPrettyPrinting.toJson(obj);
		else
			return gson.toJson(obj);
	}
	
	public static <T> void toJson(T obj, Appendable writer) throws IOException {
		Type typeOfT = new TypeToken<T>(){}.getType();
		gson.toJson(obj, typeOfT, writer);
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
	public static <T> T fromJson(String json) throws IOException, ClassCastException {
		Type typeOfT = new TypeToken<T>(){}.getType();
		T obj = gson.fromJson(json, typeOfT);
		return obj;
	}
	public static <T> T fromJson(String json, Class<T> type) throws IOException, ClassCastException {
		T obj = gson.fromJson(json, type);
		return obj;
	}
	
	public static <T> T fromJson(String json, Type type) throws IOException, ClassCastException {
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

	public static <T> String toUrlEncoding(T obj) throws IllegalAccessException {
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
				sb.append(field.get(obj).toString());
			}
		}
		return sb.toString();
	}

	public static <T> T fromUrlEncoding(String formData, Class<T> type) throws InstantiationException, IllegalAccessException, UnsupportedEncodingException {
		if (formData == null)
			return null;
		T obj = type.newInstance();
		String[] parts = formData.split("&");
		for (String part : parts) {
			String[] pair = part.split("=");
			if (pair.length != 2)
				continue;
			try {
				String key = URLDecoder.decode(pair[0], "utf-8");
				Field field = type.getField(key);
				if (field == null) 
					continue;
				String val = URLDecoder.decode(pair[1], "utf-8");
				Class<?> fieldType = field.getType();
				StringConvertor convertor = convertMapping.get(fieldType);
				if (convertor != null) {
					field.setAccessible(true);
					convertor.setValue(obj, field, val);
				}
			} catch (NoSuchFieldException err) {
			}
		}
		return obj;
	}
}
