package com.github.thorqin.toolkit.db;

import com.github.thorqin.toolkit.annotation.Service;
import com.github.thorqin.toolkit.service.IService;
import com.github.thorqin.toolkit.trace.*;
import com.github.thorqin.toolkit.utility.ConfigManager;
import com.github.thorqin.toolkit.utility.Localization;
import com.github.thorqin.toolkit.utility.Serializer;
import com.github.thorqin.toolkit.utility.StringUtils;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.validation.Validator;
import com.github.thorqin.toolkit.validation.annotation.ValidateNumber;
import com.github.thorqin.toolkit.validation.annotation.ValidateString;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import org.joda.time.DateTime;
import java.io.*;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.sql.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**********************************************************
 * DBStore implementation
 * @author nuo.qin
 *
 **********************************************************/
public final class DBService implements IService, AutoCloseable {

    public static class DBSetting {
		@ValidateString
		public String driver;
		@ValidateString
		public String uri;
		@ValidateString
		public String user;
		@ValidateString
		public String password;
		@ValidateNumber(min = 1)
		public int minConnectionsPerPartition = 5;
		@ValidateNumber(min = 1)
		public int maxConnectionsPerPartition = 20;
		@ValidateNumber(min = 1)
		public int partitionCount = 1;
		public int connectionTimeout = 0; // When get connection from pool exceed timeout value then exception
		public boolean lazyInit = true;
		public boolean trace = false;
	}

	@Service("logger")
	private Logger logger =
			Logger.getLogger(DBService.class.getName());
	private static final Map<Class<?>, Integer> typeMapping;
	// This type very depends on which database are you using.
	private static final Map<Class<?>, String> arrayType;

	private static final Map<Class<?>, String> stmtGetMapping;
	private static class StmtSet {
		public String name;
		public Class<?> type;
		public StmtSet(String name, Class<?> type) {
			this.name = name;
			this.type = type;
		}
	}
	private static final Map<Class<?>, StmtSet> stmtSetMapping;

	static {
		typeMapping = new HashMap<>();
		typeMapping.put(Blob.class, Types.BLOB);
		typeMapping.put(Clob.class, Types.CLOB);
		typeMapping.put(List.class, java.sql.Types.ARRAY);
		typeMapping.put(byte[].class, java.sql.Types.VARBINARY);
		typeMapping.put(Void.class, java.sql.Types.JAVA_OBJECT);
		typeMapping.put(void.class, java.sql.Types.JAVA_OBJECT);
		typeMapping.put(Date.class, java.sql.Types.TIMESTAMP);
		typeMapping.put(Calendar.class, java.sql.Types.TIMESTAMP);
		typeMapping.put(DateTime.class, java.sql.Types.TIMESTAMP);
		typeMapping.put(Byte.class, java.sql.Types.TINYINT);
		typeMapping.put(byte.class, java.sql.Types.TINYINT);
		typeMapping.put(Short.class, java.sql.Types.SMALLINT);
		typeMapping.put(short.class, java.sql.Types.SMALLINT);
		typeMapping.put(Integer.class, java.sql.Types.INTEGER);
		typeMapping.put(int.class, java.sql.Types.INTEGER);
		typeMapping.put(Long.class, java.sql.Types.BIGINT);
		typeMapping.put(long.class, java.sql.Types.BIGINT);
		typeMapping.put(Float.class, java.sql.Types.FLOAT);
		typeMapping.put(float.class, java.sql.Types.FLOAT);
		typeMapping.put(Double.class, java.sql.Types.DOUBLE);
		typeMapping.put(double.class, java.sql.Types.DOUBLE);
		typeMapping.put(Boolean.class, java.sql.Types.BOOLEAN);
		typeMapping.put(boolean.class, java.sql.Types.BOOLEAN);
		typeMapping.put(String.class, java.sql.Types.VARCHAR);
		typeMapping.put(DBCursor.class, java.sql.Types.OTHER);
		typeMapping.put(DBTable.class, java.sql.Types.OTHER);
		typeMapping.put(BigDecimal.class, java.sql.Types.NUMERIC);
		
		// Following type definitions are adapted for postgresql only.
		arrayType = new HashMap<>();
		arrayType.put(String[].class, "text");
		arrayType.put(byte[].class, "bytea");
		arrayType.put(Byte[].class, "bytea");
		arrayType.put(short[].class, "smallint");
		arrayType.put(Short[].class, "smallint");
		arrayType.put(int[].class, "integer");
		arrayType.put(Integer[].class, "integer");
		arrayType.put(long[].class, "bigint");
		arrayType.put(Long[].class, "bigint");
		arrayType.put(float[].class, "real");
		arrayType.put(Float[].class, "real");
		arrayType.put(double[].class, "double precision");
		arrayType.put(Double[].class, "double precision");
		arrayType.put(boolean[].class, "boolean");
		arrayType.put(Boolean[].class, "boolean");
		arrayType.put(BigDecimal[].class, "numeric");
		arrayType.put(Date[].class, "timestamp with time zone");
		arrayType.put(Calendar[].class, "timestamp with time zone");

		stmtGetMapping = new HashMap<>();
		stmtGetMapping.put(String.class, "getString");
		stmtGetMapping.put(int.class, "getInt");
		stmtGetMapping.put(Integer.class, "getInt");
		stmtGetMapping.put(long.class, "getLong");
		stmtGetMapping.put(Long.class, "getLong");
		stmtGetMapping.put(short.class, "getShort");
		stmtGetMapping.put(Short.class, "getShort");
		stmtGetMapping.put(byte.class, "getByte");
		stmtGetMapping.put(Byte.class, "getByte");
		stmtGetMapping.put(double.class, "getDouble");
		stmtGetMapping.put(Double.class, "getDouble");
		stmtGetMapping.put(float.class, "getFloat");
		stmtGetMapping.put(Float.class, "getFloat");
		stmtGetMapping.put(boolean.class, "getBoolean");
		stmtGetMapping.put(Boolean.class, "getBoolean");
		stmtGetMapping.put(BigDecimal.class, "getBigDecimal");
		stmtGetMapping.put(byte[].class, "getBytes");
		stmtGetMapping.put(Blob.class, "getBlob");
		stmtGetMapping.put(Clob.class, "getClob");

		stmtSetMapping = new HashMap<>();
		stmtSetMapping.put(String.class, new StmtSet("setString", String.class));
		stmtSetMapping.put(int.class, new StmtSet("setInt", int.class));
		stmtSetMapping.put(Integer.class, new StmtSet("setInt", int.class));
		stmtSetMapping.put(long.class, new StmtSet("setLong", long.class));
		stmtSetMapping.put(Long.class, new StmtSet("setLong", long.class));
		stmtSetMapping.put(short.class, new StmtSet("setShort", short.class));
		stmtSetMapping.put(Short.class, new StmtSet("setShort", short.class));
		stmtSetMapping.put(byte.class, new StmtSet("setByte", byte.class));
		stmtSetMapping.put(Byte.class, new StmtSet("setByte", byte.class));
		stmtSetMapping.put(double.class, new StmtSet("setDouble", double.class));
		stmtSetMapping.put(Double.class, new StmtSet("setDouble", double.class));
		stmtSetMapping.put(float.class, new StmtSet("setFloat", float.class));
		stmtSetMapping.put(Float.class, new StmtSet("setFloat", float.class));
		stmtSetMapping.put(boolean.class, new StmtSet("setBoolean", boolean.class));
		stmtSetMapping.put(Boolean.class, new StmtSet("setBoolean", boolean.class));
		stmtSetMapping.put(BigDecimal.class, new StmtSet("setBigDecimal", BigDecimal.class));
		stmtSetMapping.put(byte[].class, new StmtSet("setBytes", byte[].class));
		stmtSetMapping.put(Blob.class, new StmtSet("setBlob", Blob.class));
		stmtSetMapping.put(Clob.class, new StmtSet("setClob", Clob.class));
	}
	
	private static int toSqlType(Class<?> type) {
		Integer sqlType = typeMapping.get(type);
		if (sqlType == null) {
			if (type.isArray())
				return java.sql.Types.ARRAY; 
			else if (type.isAnnotationPresent(UDT.class))
				return java.sql.Types.STRUCT;
			else if (InputStream.class.isAssignableFrom(type))
				return java.sql.Types.BLOB;
			else
				return java.sql.Types.OTHER;
		} else
			return sqlType;
	}
	
	private static java.sql.Timestamp toSqlDate(Date date) {
		return new java.sql.Timestamp(date.getTime());
	}
	private static java.sql.Timestamp toSqlDate(Calendar calendar) {
		return new java.sql.Timestamp(calendar.getTimeInMillis());
	}
	private static java.sql.Timestamp toSqlDate(DateTime dateTime) {
		return new java.sql.Timestamp(dateTime.getMillis());
	}

	private static Object toSqlObject(Connection conn, Object obj) throws SQLException {
		Struct udt;
		if (obj == null) {
			return null;
		} else if (obj.getClass().equals(Date.class)) {
			return toSqlDate((Date)obj);
		} else if (obj.getClass().equals(Calendar.class)) {
			return toSqlDate((Calendar)obj);
		} else if (obj.getClass().equals(DateTime.class)) {
			return toSqlDate((DateTime) obj);
		} else if (obj.getClass().equals(DBCursor.class)) {
			return ((DBCursor)obj).getResultSet();
		} else if ((udt = toSqlStruct(conn, obj)) != null) {
			return udt;
		} else {
			return obj;
		}
	}

	private static Struct toSqlStruct(Connection conn, Object obj) throws SQLException {
		if (obj == null)
			return null;
		Class<?> clazz = obj.getClass();
		UDT udt = clazz.getAnnotation(UDT.class);
		if (udt == null) {
			return null;
		}
		ArrayList<Object> attributes = new ArrayList<>(clazz.getFields().length);
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(UDTField.class)) {
				try {
					attributes.add(toSqlObject(conn, field.get(obj)));
				} catch (IllegalArgumentException
						| IllegalAccessException e) {
					throw new SQLException("Convert object to java.sql.Struct failed.", e);
				}
			}
		}
		return conn.createStruct(udt.udtName(), attributes.toArray());
	}

	/**
	 * Currently only support postgresQL
	 * @param conn
	 * @param obj
	 * @return SqlType
	 * @throws SQLException
	 */
	private static Array toSqlArray(Connection conn, Object[] obj) throws SQLException {
		if (obj.getClass().isArray()) {
			String type = arrayType.get(obj.getClass());
			if (type != null)
				return conn.createArrayOf(type, obj);
			else
				return null;
		} else
			return null;
	}

	private static DateTime toDateTime(long time) {
		return new DateTime(time);
	}

	private static Date toDate(long time) {
		return new Date(time);
	}

	@SuppressWarnings("unchecked")
	private static <T> T stmtGet(Object stmt,
                                 Class<T> valueType,
                                 int offset,
                                 Map<String, Class<?>> udtMapping)
			throws SQLException {
		Class<?> cls = stmt.getClass();
		try {
            T value;
            Method wasNull = cls.getMethod("wasNull");
            wasNull.setAccessible(true);
			String methodName = stmtGetMapping.get(valueType);
			if (methodName != null) {
				Method method = cls.getMethod(methodName, int.class);
				method.setAccessible(true);
				value = (T)method.invoke(stmt, offset);
                if ((boolean)wasNull.invoke(stmt))
                    return null;
                else
                    return value;
			} else if (valueType.equals(DateTime.class)) {
				Method method = cls.getMethod("getTimestamp", int.class);
				method.setAccessible(true);
				Timestamp timestamp = (Timestamp)method.invoke(stmt, offset);
                if (timestamp == null)
                    return null;
				return (T)new DateTime(timestamp.getTime());
			} else if (valueType.equals(Date.class)) {
				Method method =cls.getMethod("getTimestamp", int.class);
				method.setAccessible(true);
				Timestamp timestamp = (Timestamp)method.invoke(stmt, offset);
                if (timestamp == null)
                    return null;
				return (T)new Date(timestamp.getTime());
			} else if (valueType.equals(DBTable.class)) {
				Method method =cls.getMethod("getObject", int.class);
				method.setAccessible(true);
				try (DBCursor cursor = (DBCursor)fromSqlObject(method.invoke(stmt, offset), udtMapping)) {
                    if (cursor == null)
                        return null;
                    return (T) cursor.getTable();
                }
			} else if (valueType.equals(DBCursor.class)) {
				Method method =cls.getMethod("getObject", int.class);
				method.setAccessible(true);
				return (T) fromSqlObject(method.invoke(stmt, offset), udtMapping);
			} else if (InputStream.class.isAssignableFrom(valueType)) {
				Method method = cls.getMethod("getBlob", int.class);
				method.setAccessible(true);
				Blob blob = (Blob)method.invoke(stmt, offset);
                if (blob == null)
                    return null;
				return (T)blob.getBinaryStream();
			} else {
				Method method = cls.getMethod("getObject", int.class);
				method.setAccessible(true);
				return (T) fromSqlObject(method.invoke(stmt, offset), valueType, udtMapping);
			}
		} catch (Exception ex) {
			throw new SQLException(ex);
		}
	}
	private static <T> void stmtSet(Object stmt,
									Connection conn,
									Class<T> paramType,
									int offset,
									Object value) throws SQLException {
		Class<?> cls = stmt.getClass();
		try {
			StmtSet stmtInfo = stmtSetMapping.get(paramType);
			Struct udt;
			if (stmtInfo != null) {
				Method method = cls.getMethod(stmtInfo.name, int.class, stmtInfo.type);
				method.setAccessible(true);
				method.invoke(stmt, offset, value);
			} else if (paramType.equals(DateTime.class)) {
				Method method = cls.getMethod("setTimestamp", int.class, Timestamp.class);
				method.setAccessible(true);
				method.invoke(stmt, offset, toSqlDate((DateTime) value));
			} else if (paramType.equals(Date.class)) {
				Method method = cls.getMethod("setTimestamp", int.class, Timestamp.class);
				method.setAccessible(true);
				method.invoke(stmt, offset, toSqlDate((Date) value));
			} else if (paramType.equals(Calendar.class)) {
				Method method = cls.getMethod("setTimestamp", int.class, Timestamp.class);
				method.setAccessible(true);
				method.invoke(stmt, offset, toSqlDate((Calendar) value));
			} else if (paramType.equals(DBCursor.class)) {
				cls.getMethod("setObject", int.class, Object.class)
						.invoke(stmt, offset, ((DBCursor) value).getResultSet());
			} else if (paramType.isArray()) {
				Array array = toSqlArray(conn, (Object[]) value);
				Method method = cls.getMethod("setArray", int.class, Array.class);
				method.setAccessible(true);
				method.invoke(stmt, offset, array);
			} else if (InputStream.class.isAssignableFrom(paramType)) {
				Method method = cls.getMethod("setBlob", int.class, InputStream.class);
				method.setAccessible(true);
				method.invoke(stmt, offset, value);
			} else if ((udt = toSqlStruct(conn, value)) != null) {
				Method method = cls.getMethod("setObject", int.class, Object.class, int.class);
				method.setAccessible(true);
				method.invoke(stmt, offset, udt, java.sql.Types.STRUCT);
			} else  {
				Method method = cls.getMethod("setObject", int.class, Object.class);
				method.setAccessible(true);
				method.invoke(stmt, offset, value);
			}
		} catch (Exception ex) {
			throw new SQLException(ex);
		}
	}

	private static Object fromSqlObject(Object obj, Class<?> destType, Map<String, Class<?>> udtMapping) throws SQLException {
		if (obj == null)
			return null;
		Class<?> type = obj.getClass();
		if (destType.equals(Date.class) && type.equals(java.sql.Timestamp.class)) {
			return toDate(((java.sql.Timestamp)obj).getTime());
		} else if (destType.equals(Date.class) && type.equals(java.sql.Date.class)) {
			return toDate(((java.sql.Date)obj).getTime());
		} else if (destType.equals(Date.class) && type.equals(java.sql.Time.class)) {
			return toDate(((java.sql.Time)obj).getTime());
		} else if (destType.equals(DateTime.class) && type.equals(java.sql.Timestamp.class)) {
			return toDateTime(((java.sql.Timestamp)obj).getTime());
		} else if (destType.equals(DateTime.class) && type.equals(java.sql.Date.class)) {
			return toDateTime(((java.sql.Date)obj).getTime());
		} else if (destType.equals(DateTime.class) && type.equals(java.sql.Time.class)) {
			return toDateTime(((java.sql.Time) obj).getTime());
		} else if (destType.equals(List.class) && type.equals(Array.class)) {
			Array array = (Array)obj;
			List<Object> list = new LinkedList<>();
			try (ResultSet arrayResult = array.getResultSet()) {
				while (arrayResult.next()) {
					list.add(fromSqlObject(arrayResult.getObject(2), udtMapping));
				}
				return list;
			}
		} else if (destType.equals(DBCursor.class) && obj instanceof ResultSet) {
			return new DBCursor((ResultSet)obj);
		} else if (destType.equals(Object[].class) && obj instanceof Array) {
			Array array = (Array)obj;
			List<Object> list = new LinkedList<>();
			try (ResultSet arrayResult = array.getResultSet()){
				while (arrayResult.next()) {
					list.add(fromSqlObject(arrayResult.getObject(2), udtMapping));
				}
				return list.toArray();
			} 
		} else if (type.equals(Struct.class)) {
			UDT udt = destType.getAnnotation(UDT.class);
			if (udt == null)
				throw new SQLException("Cannot map UDT object");
			Struct struct = (Struct)obj;
			if (!udt.udtName().equals(struct.getSQLTypeName())) {
				throw new SQLException("Cannot convert UDT object from "
						+ struct.getSQLTypeName() + " to " + udt.udtName());
			}
			Object[] attributes = struct.getAttributes();
			Object instance;
			try {
				instance = destType.newInstance();
			} catch (InstantiationException
					| IllegalAccessException e) {
				throw new SQLException("Parse java.sql.Struct failed.", e);
			}
			Field[] fields = destType.getDeclaredFields();
			for (int i = 0, j = 0; i < fields.length && j < attributes.length; i++) {
				if (fields[i].isAnnotationPresent(UDTField.class)) {
					try {
						fields[i].setAccessible(true);
						fields[i].set(instance, fromSqlObject(attributes[j], fields[i].getType(), udtMapping));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new SQLException("Parse java.sql.Struct failed.", e);
					}
					j++;
				}
			}
			return instance;
		} else {
			return obj;
		}
	}

	private static Object fromSqlObject(Object sqlObj, Map<String, Class<?>> udtMapping) throws SQLException {
		if (sqlObj == null)
			return null;
		Class<?> type = sqlObj.getClass();
		if (type.equals(java.sql.Timestamp.class)) {
			return toDateTime(((java.sql.Timestamp)sqlObj).getTime());
		} else if (type.equals(java.sql.Date.class)) {
			return toDateTime(((java.sql.Date)sqlObj).getTime());
		} else if (type.equals(java.sql.Time.class)) {
			return toDateTime(((java.sql.Time)sqlObj).getTime());
		} else if (sqlObj instanceof ResultSet) {
			return new DBCursor((ResultSet)sqlObj);
		} else if (sqlObj instanceof Array) {
			Array array = (Array)sqlObj;
			List<Object> list = new LinkedList<>();
			try (ResultSet arrayResult = array.getResultSet()) {
				while (arrayResult.next()) {
					list.add(fromSqlObject(arrayResult.getObject(2), udtMapping));
				}
				return list;
			} 
		} else if (sqlObj instanceof Struct) {
			Struct struct = (Struct)sqlObj;
			Class<?> clazz = udtMapping.get(struct.getSQLTypeName());
			if (clazz != null) {
				Object[] attributes = struct.getAttributes();
				Object instance = null;
				try {
					instance = clazz.newInstance();
				} catch (InstantiationException
						| IllegalAccessException e) {
					throw new SQLException("Parse java.sql.Struct failed.", e);
				}
				Field[] fields = clazz.getFields();
				for (int i = 0, j = 0; i < fields.length && j < attributes.length; i++) {
					if (fields[i].isAnnotationPresent(UDTField.class)) {
						try {
							fields[i].setAccessible(true);
							fields[i].set(instance, fromSqlObject(attributes[j], fields[i].getType(), udtMapping));
						} catch (IllegalArgumentException | IllegalAccessException e) {
							throw new SQLException("Parse java.sql.Struct failed.", e);
						}
						j++;
					}
				}
				return instance;
			} else {
				List<Object> list = new LinkedList<>();
				for (Object attribute : struct.getAttributes()) {
					list.add(fromSqlObject(attribute, udtMapping));
				}
				return list;
			}
		} else {
			return sqlObj;
		}
	}
			
	
	
	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}
	
	public static class DBTable {
		public String[] head;
		public List<Object[]> data;
		public Integer length;
		private Map<String, Integer> headMapping = null;
		private void buildHeadMapping() {
			if (headMapping == null) {
				headMapping = new HashMap<>();
				for(int i = 0; i < head.length; i++) {
					String colName = head[i];
					headMapping.put(colName, i);
				}
			}
		}
		public int getColumnPos(String column) {
			buildHeadMapping();
			return headMapping.get(column);
		}
		public Object getValue(Object[] row, String column) {
			buildHeadMapping();
			Integer pos = headMapping.get(column);
			if (pos != null) {
				return row[pos];
			} else
				throw new InvalidParameterException("Column '" + column + "' doesn't exist!");
		}
        public Object getValue(int row, String column) {
            buildHeadMapping();
            Integer pos = headMapping.get(column);
            if (pos != null) {
                return data.get(row)[pos];
            } else
                throw new InvalidParameterException("Column '" + column + "' doesn't exist!");
        }
        public Object getValue(int row, int column) {
            if (column >= 0 && column < head.length) {
                return data.get(row)[column];
            } else
                throw new InvalidParameterException("Column '" + column + "' doesn't exist!");
        }
		public void setValue(Object[] row, String column, Object value) {
			buildHeadMapping();
			Integer pos = headMapping.get(column);
			if (pos != null) {
				row[pos] = value;
			} else
				throw new InvalidParameterException("Column '" + column + "' doesn't exist!");
		}
        public void setValue(int row, String column, Object value) {
            buildHeadMapping();
            Integer pos = headMapping.get(column);
            if (pos != null) {
                data.get(row)[pos] = value;
            } else
                throw new InvalidParameterException("Column '" + column + "' doesn't exist!");
        }
        public void setValue(int row, int column, Object value) {
            if (column >= 0 && column < head.length) {
                data.get(row)[column] = value;
            } else
                throw new InvalidParameterException("Column '" + column + "' doesn't exist!");
        }

		public List<Map<String, Object>> toList() {
			List<Map<String, Object>> list = new ArrayList<>(data.size());
			for (Object[] row: data) {
				Map<String, Object> obj = new HashMap<>();
				for (int i = 0; i < head.length; i++) {
					if (i < row.length)
						obj.put(head[i], row[i]);
					else
						obj.put(head[i], null);
				}
				list.add(obj);
			}
			return list;
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface UDT {
		String udtName() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface UDTField {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface DBField {
		String value() default "";
	}

	public static class DBOut<T> {
		protected T value;
		protected Class<T> type;

		public DBOut(Class<T> type) {
			this.type = type;
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}
		public Class<T> getType() {
			return type;
		}
	}

	public static class DBRef<T> extends DBOut<T> {
		public DBRef(T value, Class<T> type) {
			super(type);
			this.value = value;
		}
	}
	
	public static class DBOutString extends DBOut<String> {
		public DBOutString() {
			super(String.class);
		}
	}
	public static class DBOutInteger extends DBOut<Integer> {
		public DBOutInteger() {
			super(Integer.class);
		}
	}
	public static class DBOutShort extends DBOut<Short> {
		public DBOutShort() {
			super(Short.class);
		}
	}
	public static class DBOutLong extends DBOut<Long> {
		public DBOutLong() {
			super(Long.class);
		}
	}
	public static class DBOutByte extends DBOut<Byte> {
		public DBOutByte() {
			super(Byte.class);
		}
	}
	public static class DBOutFloat extends DBOut<Float> {
		public DBOutFloat() {
			super(Float.class);
		}
	}
	public static class DBOutDouble extends DBOut<Double> {
		public DBOutDouble() {
			super(Double.class);
		}
	}
	public static class DBOutDate extends DBOut<Date> {
		public DBOutDate() {
			super(Date.class);
		}
	}
	public static class DBOutDateTime extends DBOut<DateTime> {
		public DBOutDateTime() {
			super(DateTime.class);
		}
	}
	public static class DBOutBoolean extends DBOut<Boolean> {
		public DBOutBoolean() {
			super(Boolean.class);
		}
	}
	public static class DBOutTable extends DBOut<DBTable> {
		public DBOutTable() {
			super(DBTable.class);
		}
	}
	public static class DBOutCursor extends DBOut<DBCursor> {
		public DBOutCursor() {
			super(DBCursor.class);
		}
	}
	public static class DBOutArray extends DBOut<Object[]> {
		public DBOutArray() {
			super(Object[].class);
		}
	}
	public static class DBOutBinary extends DBOut<byte[]> {
		public DBOutBinary() {
			super(byte[].class);
		}
	}
	
	public static class DBRefString extends DBRef<String> {
		public DBRefString(String value) {
			super(value, String.class);
		}
		public DBRefString() {
			super(null, String.class);
		}
	}
	public static class DBRefInteger extends DBRef<Integer> {
		public DBRefInteger(Integer value) {
			super(value, Integer.class);
		}
		public DBRefInteger() {
			super(null, Integer.class);
		}
	}
	public static class DBRefShort extends DBRef<Short> {
		public DBRefShort(Short value) {
			super(value, Short.class);
		}
		public DBRefShort() {
			super(null, Short.class);
		}
	}
	public static class DBRefLong extends DBRef<Long> {
		public DBRefLong(Long value) {
			super(value, Long.class);
		}
		public DBRefLong() {
			super(null, Long.class);
		}
	}
	public static class DBRefByte extends DBRef<Byte> {
		public DBRefByte(Byte value) {
			super(value, Byte.class);
		}
		public DBRefByte() {
			super(null, Byte.class);
		}
	}
	public static class DBRefFloat extends DBRef<Float> {
		public DBRefFloat(Float value) {
			super(value, Float.class);
		}
		public DBRefFloat() {
			super(null, Float.class);
		}
	}
	public static class DBRefDouble extends DBRef<Double> {
		public DBRefDouble(Double value) {
			super(value, Double.class);
		}
		public DBRefDouble() {
			super(null, Double.class);
		}
	}
	public static class DBRefDate extends DBRef<Date> {
		public DBRefDate(Date value) {
			super(value, Date.class);
		}
		public DBRefDate() {
			super(null, Date.class);
		}
	}
	public static class DBRefDateTime extends DBRef<DateTime> {
		public DBRefDateTime(DateTime value) {
			super(value, DateTime.class);
		}
		public DBRefDateTime() {
			super(null, DateTime.class);
		}
	}
	public static class DBRefBoolean extends DBRef<Boolean> {
		public DBRefBoolean(Boolean value) {
			super(value, Boolean.class);
		}
		public DBRefBoolean() {
			super(null, Boolean.class);
		}
	}
	public static class DBRefTable extends DBRef<DBTable> {
		public DBRefTable(DBTable value) {
			super(value, DBTable.class);
		}
		public DBRefTable() {
			super(null, DBTable.class);
		}
	}
	public static class DBRefCursor extends DBRef<DBCursor> {
		public DBRefCursor(DBCursor value) {
			super(value, DBCursor.class);
		}
		public DBRefCursor() {
			super(null, DBCursor.class);
		}
	}
	public static class DBRefArray extends DBRef<Object[]> {
		public DBRefArray(Object[] value) {
			super(value, Object[].class);
		}
		public DBRefArray() {
			super(null, Object[].class);
		}
	}
	public static class DBRefBinary extends DBRef<byte[]> {
		public DBRefBinary(byte[] value) {
			super(value, byte[].class);
		}
		public DBRefBinary() {
			super(null, byte[].class);
		}
	}

	public interface DBResultHanlder {
		void handle(ResultSet result) throws Exception;
	}

	public interface RowTypeAdapter<T> {
		void make(T obj) throws SQLException;
	}

    public interface TableAdjuster {
        String[] adjustHead(String[] head);
        Object[] adjustLine(Object[] line);
    }

	public static class DBCursor implements AutoCloseable {
		private Statement statement;
		private ResultSet resultSet;
		private String[] columns = new String[0];
		private Map<String, Integer> columnMap = new HashMap<>();

		private void buildColumnMap() throws SQLException {
			columnMap.clear();
			if (resultSet == null) {
				columns = new String[0];
			}
			ResultSetMetaData mataData = resultSet.getMetaData();
			int columnCount = resultSet.getMetaData().getColumnCount();
			columns = new String[columnCount];
			for (int i = 1; i <= columnCount; i++) {
				String colName = mataData.getColumnLabel(i);
				columns[i - 1] = colName;
				if (!columnMap.containsKey(colName.toLowerCase()))
					columnMap.put(colName.toLowerCase(), i);
			}
		}

		public DBCursor() {
			resultSet = null;
			statement = null;
		}
		public DBCursor(ResultSet resultSet, Statement statement) throws SQLException {
			this.resultSet = resultSet;
			this.statement = statement;
			buildColumnMap();
		}
		public DBCursor(ResultSet resultSet) throws SQLException {
			this.resultSet = resultSet;
			buildColumnMap();
		}
		public String[] getColumns() {
			return columns;
		}
		public boolean next() throws SQLException {
			if (resultSet == null)
				return false;
			return resultSet.next();
		}
		public boolean previous() throws SQLException {
			if (resultSet == null)
				return false;
			return resultSet.previous();
		}
		public void beforeFirst() throws SQLException {
			resultSet.beforeFirst();
		}
		public void afterLast() throws SQLException {
			resultSet.afterLast();
		}
		public boolean absolute(int row) throws SQLException {
			return resultSet.absolute(row);
		}
		@SuppressWarnings("unchecked")
		public <T> T getValue(int column) throws SQLException {
			return (T)fromSqlObject(resultSet.getObject(column), null);
		}
		@SuppressWarnings("unchecked")
		public <T> T getValue(int column, Map<String, Class<?>> udtMapping) throws SQLException {
			return (T)fromSqlObject(resultSet.getObject(column), udtMapping);
		}
		@SuppressWarnings("unchecked")
		public <T> T getValue(String columnName) throws SQLException {
			return (T)getValue(columnName, (Map<String, Class<?>>)null);
		}
		@SuppressWarnings("unchecked")
		public <T> T getValue(String columnName, Map<String, Class<?>> udtMapping) throws SQLException {
			Integer idx = columnMap.get(columnName.toLowerCase());
			if (idx == null)
				return null;
			return (T)fromSqlObject(resultSet.getObject(idx), udtMapping);
		}
		@SuppressWarnings("unchecked")
		public <T> T getValue(int column, Class<T> type) throws SQLException {
			return stmtGet(resultSet, type, column, null);
		}
		@SuppressWarnings("unchecked")
		public <T> T getValue(int column, Class<T> type, Map<String, Class<?>> udtMapping) throws SQLException {
			return stmtGet(resultSet, type, column, udtMapping);
		}
		@SuppressWarnings("unchecked")
		public <T> T getValue(String columnName, Class<T> type) throws SQLException {
			return (T)getValue(columnName, type, null);
		}
		@SuppressWarnings("unchecked")
		public <T> T getValue(String columnName, Class<T> type, Map<String, Class<?>> udtMapping) throws SQLException {
			Integer idx = columnMap.get(columnName.toLowerCase());
			if (idx == null)
				return null;
			return stmtGet(resultSet, type, idx, udtMapping);
		}

		public ResultSet getResultSet() {
			return resultSet;
		}
		public void setResultSet(ResultSet resultSet) throws SQLException {
			this.resultSet = resultSet;
			statement = null;
			buildColumnMap();
		}
		public void perform(DBResultHanlder handler) throws Exception {
			if (resultSet != null)
				handler.handle(resultSet);
		}
		public <T> List<T> getList(Class<T> type) throws IllegalAccessException, SQLException, InstantiationException {
			return getList(type, null, null);
		}

		public <T> List<T> getList(Class<T> type, RowTypeAdapter<T> adapter) throws IllegalAccessException, SQLException, InstantiationException {
			return getList(type, adapter, null);
		}

		public <T> List<T> getList(Class<T> type, RowTypeAdapter<T> adapter, Map<String, Class<?>> udtMapping) throws IllegalAccessException, InstantiationException, SQLException {
			List<T> list = new LinkedList<>();
			if (resultSet == null)
				return list;
			while (resultSet.next()) {
				T obj = get(type, adapter, udtMapping);
				list.add(obj);
			}
			return list;
		}

		public List<Map<String, Object>> getList() throws SQLException {
			List<Map<String, Object>> list = new LinkedList<>();
			if (resultSet == null)
				return list;
			while (resultSet.next()) {
				Map<String, Object> obj = get();
				list.add(obj);
			}
			return list;
		}

        public <T> T get(Class<T> type) throws IllegalAccessException, InstantiationException, SQLException {
            return get(type, null, null);
        }

        public <T> T get(Class<T> type, RowTypeAdapter<T> adapter) throws IllegalAccessException, InstantiationException, SQLException {
            return get(type, adapter, null);
        }

        public <T> T get(Class<T> type, RowTypeAdapter<T> adapter, Map<String, Class<?>> udtMapping) throws IllegalAccessException, InstantiationException, SQLException {
            if (resultSet == null)
                return null;
            T obj = type.newInstance();
            for (Field field : type.getDeclaredFields()) {
                DBField anno = field.getAnnotation(DBField.class);
                if (anno == null)
                    continue;
                String colName = anno.value();
                if (colName.isEmpty()) {
                    colName = StringUtils.camelToUnderline(field.getName());
                }
                Integer col = null;
                for (int i = 0; i < columns.length; i++) {
                    if (columns[i].equalsIgnoreCase(colName)) {
                        col = i + 1;
                        break;
                    }
                }
                if (col != null) {
                    Class<?> fieldType = field.getType();
                    field.setAccessible(true);
                    field.set(obj, stmtGet(resultSet, fieldType, col, udtMapping));
                }
            }
            if (adapter != null) {
                adapter.make(obj);
            }
            return obj;
        }

		public Map<String, Object> get() throws SQLException {
			if (resultSet == null)
				return null;
			Map<String, Object> obj = new HashMap<>();
			for (int i = 0; i < columns.length; i++) {
				obj.put(columns[i], getValue(i + 1));
			}
			return obj;
		}

		public DBTable getTable() throws SQLException {
			return getTable(null, null);
		}
        public DBTable getTable(TableAdjuster adjuster) throws SQLException {
            return getTable(adjuster, null);
        }
		public DBTable getTable(TableAdjuster adjuster, Map<String, Class<?>> udtMapping) throws SQLException {
			if (resultSet == null)
				return null;
			DBTable table = new DBTable();
			table.head = Arrays.copyOf(columns, columns.length);
            if (adjuster != null)
                table.head = adjuster.adjustHead(table.head);
			LinkedList<Object[]> list = new LinkedList<>();
			while (resultSet.next()) {
				Object[] line = new Object[columns.length];
				for (int i = 1; i <= columns.length; i++) {
					line[i - 1] = fromSqlObject(resultSet.getObject(i), udtMapping);
				}
                if (adjuster != null)
                    line = adjuster.adjustLine(line);
                if (line != null)
				    list.add(line);
			}
			table.data = list;
			table.length = list.size();
			return table;
		}

		@Override
		public void close() {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ex) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ex) {
				}
			}
		}
		@Override
		protected void finalize() throws Throwable {
			close();
			super.finalize();
		}
	}
	

	public interface DBWork {
		void doWork(DBSession session) throws Exception;
	}


    @Service("tracer")
    private Tracer tracer = null;

	/* Private properties. */
	private BoneCPConfig boneCPConfig;
	private BoneCP boneCP = null;
	private DBSetting setting;
	private Long lastInitTime = null;
    private String serviceName = null;

	public synchronized void setTracer(Tracer tracer) {
		this.tracer = tracer;
	}

    @Override
    public synchronized void start() {
        if (setting == null) {
            throw new ServiceConfigurationError(
                    MessageFormat.format("Start DBService failed: DBSettings must be provided! (Service Name: {0})", serviceName));
        }
        if (boneCPConfig != null) {
            logger.log(Level.WARNING, "DBService has already started! (Service Name: {0})", serviceName);
            return;
        }
        try {
            Class.forName(setting.driver);
            boneCPConfig = new BoneCPConfig();
            boneCPConfig.setDefaultAutoCommit(true);
            boneCPConfig.setJdbcUrl(setting.uri);
            boneCPConfig.setJdbcUrl(setting.uri);
            boneCPConfig.setUsername(setting.user);
            boneCPConfig.setPassword(setting.password);
            boneCPConfig.setMinConnectionsPerPartition(setting.minConnectionsPerPartition);
            boneCPConfig.setMaxConnectionsPerPartition(setting.maxConnectionsPerPartition);
            boneCPConfig.setPartitionCount(setting.partitionCount);
            boneCPConfig.setConnectionTimeoutInMs(setting.connectionTimeout);
            boneCPConfig.setLazyInit(false); // For some reason I didn't use this feature.
            if (!setting.lazyInit) {
                init();
            }
        } catch (Exception ex) {
            boneCPConfig = null;
            throw new ServiceConfigurationError("Initialize DB Service failed. ", ex);
        }
    }

    @Override
    public void stop() {
        close();
    }

    @Override
    public synchronized boolean isStarted() {
        return boneCPConfig != null;
    }

    @Override
    public boolean config(ConfigManager configManager, String serviceName, boolean isReConfig) {
        this.serviceName = serviceName;
        DBService.DBSetting newSetting = configManager.get(serviceName, DBService.DBSetting.class);
        try {
            validateSetting(newSetting);
        } catch (ValidateException ex) {
            logger.log(Level.SEVERE, "Invalid DB configuration settings: {0}", ex.getMessage());
            return false;
        }
        boolean needRestart = !Serializer.equals(newSetting, setting);
        setting = newSetting;
        return needRestart;
    }

    public DBService() {
        this.tracer = null;
        this.setting = null;
    }

	public DBService(DBSetting dbSetting) throws ValidateException {
		this(dbSetting, null);
	}

	public DBService(DBSetting dbSetting, Tracer tracer) throws ValidateException {
        validateSetting(dbSetting);
		this.tracer = tracer;
		this.setting = dbSetting;
	}

    private void validateSetting(DBSetting dbSetting) throws ValidateException {
        Validator validator = new Validator(Localization.getInstance());
        validator.validateObject(dbSetting, DBSetting.class, false);
    }

	private void init() throws SQLException {
		boneCP = new BoneCP(boneCPConfig);
	}

    @Override
	public synchronized void close() {
		try {
            if (boneCP != null) {
                boneCP.close();
                boneCP = null;
            }
            boneCPConfig = null;
            logger.log(Level.INFO, "DBService stopped! (Service Name: {0})", serviceName);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Shutdown boneCP failed!", ex);
		}
	}

	private final ThreadLocal<Stack<Connection>> threadLocal = new ThreadLocal<>();

	public static class PreparedInfo {
		public String sql;
		public Object[] args;
	}

	private static List<String> scanSQL(String sql) {
		char[] array =  sql.toCharArray();
		int quote = 0;
		List<String> result = new LinkedList<>();
		StringBuilder sb = new StringBuilder();
		for (char c: array) {
			if (quote == 0) {
				if (c == '\'') {
					quote = 1;
				} else if (c == '"') {
					quote = 2;
				} else if (c == '?') {
					result.add(sb.toString());
					result.add("?");
					sb = new StringBuilder();
					continue;
				}
			} else if (quote == 1) {
				if (c == '\'') {
					quote = 0;
				}
			} else if (quote == 2) {
				if (c == '"') {
					quote = 0;
				}
			}
			sb.append(c);
		}
		if (sb.length() > 0)
			result.add(sb.toString());
		return result;
	}

	private static String arrayToString(Object obj) {
		if (Iterable.class.isInstance(obj)) {
			StringBuilder sb = new StringBuilder();
			Iterator<?> it = ((Iterable<?>)obj).iterator();
			boolean isFirst = true;
			while (it.hasNext()) {
				if (!isFirst)
					sb.append(",");
				Object value = it.next();
				Class<?> valueType = value.getClass();
				if (valueType.isPrimitive()
						|| valueType.equals(Integer.class)
						|| valueType.equals(Long.class)
						|| valueType.equals(Float.class)
						|| valueType.equals(Double.class)
						|| valueType.equals(Short.class)
						|| valueType.equals(Byte.class)) {
					sb.append(value);
				} else {
					sb.append("'");
					sb.append(value.toString().replaceAll("'", "''"));
					sb.append("'");
				}
				isFirst = false;
			}
			return sb.toString();
		} else { // must be an Array
			StringBuilder sb = new StringBuilder();
			boolean isFirst = true;
			if (obj.getClass().equals(byte[].class)) {
				for (byte value: (byte[])obj) {
					if (!isFirst)
						sb.append(",");
					sb.append(value);
					isFirst = false;
				}
			} else if (obj.getClass().equals(short[].class)) {
				for (short value: (short[])obj) {
					if (!isFirst)
						sb.append(",");
					sb.append(value);
					isFirst = false;
				}
			} else if (obj.getClass().equals(int[].class)) {
				for (int value: (int[])obj) {
					if (!isFirst)
						sb.append(",");
					sb.append(value);
					isFirst = false;
				}
			} else if (obj.getClass().equals(long[].class)) {
				for (long value: (long[])obj) {
					if (!isFirst)
						sb.append(",");
					sb.append(value);
					isFirst = false;
				}
			} else if (obj.getClass().equals(float[].class)) {
				for (float value: (float[])obj) {
					if (!isFirst)
						sb.append(",");
					sb.append(value);
					isFirst = false;
				}
			} else if (obj.getClass().equals(double[].class)) {
				for (double value: (double[])obj) {
					if (!isFirst)
						sb.append(",");
					sb.append(value);
					isFirst = false;
				}
			} else {
				for (Object value: (Object[])obj) {
					if (!isFirst)
						sb.append(",");
					Class<?> valueType = value.getClass();
					if (valueType.equals(Integer.class)
							|| valueType.equals(Long.class)
							|| valueType.equals(Float.class)
							|| valueType.equals(Double.class)
							|| valueType.equals(Short.class)
							|| valueType.equals(Byte.class)) {
						sb.append(value);
					} else {
						sb.append("'");
						sb.append(value.toString().replaceAll("'", "''"));
						sb.append("'");
					}
					isFirst = false;
				}
			}
			return sb.toString();
		}
	}

	public static PreparedInfo prepareSql(String sql, Object[] args) {
		PreparedInfo info = new PreparedInfo();
		List<String> placeholders = null;
		List<Object> newArgs = new ArrayList<>(args.length);
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg != null && (arg.getClass().isArray() ||
					Iterable.class.isInstance(arg))) {
				if (placeholders == null)
					placeholders = scanSQL(sql);
				if (i * 2 + 1 < placeholders.size()) {
					placeholders.set(i * 2 + 1, arrayToString(arg));
				} else
					throw new RuntimeException("SQL parameters count does not match placeholders count.");
			} else {
				newArgs.add(arg);
			}
		}
		if (placeholders == null)
			info.sql = sql;
		else
			info.sql = StringUtils.join(placeholders, "");
		info.args = newArgs.toArray();
		return info;
	}
	
	public static class DBSession implements AutoCloseable {
		final protected Connection conn;
		final protected Tracer tracer;
		final Logger logger;
        private String serviceName = null;
		public DBSession(Connection conn, Logger logger) throws SQLException {
			this(conn, null, logger);
		}
		public DBSession(Connection conn, Tracer tracer, Logger logger) throws SQLException {
			this.conn = conn;
			this.conn.setAutoCommit(true);
			this.logger = logger;
			this.tracer = tracer;
		}
        void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
		private void trace(Tracer.Info info) {
			try {
				if (tracer != null)
					tracer.trace(info);
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Record trace info failed.", ex);
			}
		}
		public void setAutoCommit(boolean autoCommit) throws SQLException {
			conn.setAutoCommit(autoCommit);
		}
		public boolean getAutoCommit() throws SQLException {
			return conn.getAutoCommit();
		}
		public void commit() throws SQLException {
			conn.commit();				
		}
		public void rollback() throws SQLException {
			conn.rollback();
		}
		@Override
		public void close()	{
			try {
                if (conn.isClosed())
                    return;
				if (!conn.getAutoCommit()) {
					try {
						conn.rollback();
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				}
				conn.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

		@Deprecated
		public Connection getConnection() {
			return conn;
		}

		@Override
		protected void finalize() throws Throwable {
			try {
				close();
			} catch (Exception ex) {
			}
			super.finalize();
		}

		@SuppressWarnings("unchecked")
		public <T> T getProxy(Class<T> interfaceType) {
			Object instance = Proxy.newProxyInstance(
					DBProxy.class.getClassLoader(),
					new Class<?>[]{interfaceType},
					new DBProxy(this));
			return (T)instance;
		}



		@SuppressWarnings("rawtypes")
		private void bindParameter(PreparedStatement stmt, Object[] args, int offset) throws SQLException {
			if (args == null)
				return;
			for (Object obj : args) {
				if (obj == null) {
					stmt.setNull(offset++, java.sql.Types.NULL);
				} else {
					Class<?> paramType = obj.getClass();
					stmtSet(stmt, conn, paramType, offset++, obj);
				}
			}
		}
		
		@SuppressWarnings("rawtypes")
		private void bindParameter(CallableStatement stmt, Object[] args, int offset) throws SQLException {
			if (args == null)
				return;
			for (Object obj : args) {
				if (obj == null) {
					stmt.setNull(offset++, java.sql.Types.NULL);
				} else {
					Class<?> paramType = obj.getClass();
					if (DBRef.class.isAssignableFrom(paramType)) {
						stmt.registerOutParameter(offset, toSqlType(((DBOut)obj).getType()));
						obj = ((DBOut)obj).getValue();
						if (obj == null)
							stmt.setNull(offset++, java.sql.Types.NULL);
						else {
							Class<?> refType = obj.getClass();
							stmtSet(stmt, conn, refType, offset++, obj);
						}
					} else if (DBOut.class.isAssignableFrom(paramType)) {
						stmt.registerOutParameter(offset++, toSqlType(((DBOut)obj).getType()));
					} else {
						stmtSet(stmt, conn, paramType, offset++, obj);
					}
				}
			}
		}

        public PreparedStatement prepareStatement(String sql) throws SQLException {
            return conn.prepareStatement(sql);
        }

		public int execute(String queryString, Object... args) throws SQLException {
			long beginTime = System.currentTimeMillis();
			boolean success = true;
			PreparedInfo preparedInfo = prepareSql(queryString, args);
			try (PreparedStatement stmt = conn.prepareStatement(preparedInfo.sql)){
				bindParameter(stmt, preparedInfo.args, 1);
				return stmt.executeUpdate();
			} catch (Exception ex) {
				success = false;
				throw ex;
			} finally {
				if (tracer != null) {
					Tracer.Info info = new Tracer.Info();
					info.catalog = "database";
					info.name = "execute";
                    info.put("serviceName", serviceName);
					info.put("statement", queryString);
					info.put("success", success);
					info.put("startTime", beginTime);
					info.put("runningTime", System.currentTimeMillis() - beginTime);
					trace(info);
				}
			}
		}

		public int execute(PreparedStatement stmt, Object... args) throws SQLException {
			long beginTime = System.currentTimeMillis();
			boolean success = true;
			try {
				bindParameter(stmt, args, 1);
				return stmt.executeUpdate();
			} catch (Exception ex) {
				success = false;
				throw ex;
			} finally {
				if (tracer != null) {
					Tracer.Info info = new Tracer.Info();
					info.catalog = "database";
					info.name = "execute";
                    info.put("serviceName", serviceName);
					info.put("statement", "PreparedStatement");
					info.put("success", success);
					info.put("startTime", beginTime);
					info.put("runningTime", System.currentTimeMillis() - beginTime);
					trace(info);
				}
			}
		}
		
		public DBCursor query(String queryString,
				Object... args) throws SQLException {
			long beginTime = System.currentTimeMillis();
			boolean success = true;
			PreparedInfo preparedInfo = prepareSql(queryString, args);
			PreparedStatement stmt = conn.prepareStatement(preparedInfo.sql);
			ResultSet rs = null;
			try {
				bindParameter(stmt, preparedInfo.args, 1);
				rs = stmt.executeQuery();
				return new DBCursor(rs, stmt);
			} catch (Exception ex) {
				success = false;
				if (rs != null) {
					try {
						rs.close();
					} catch (SQLException e) {}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {}
				}
				throw ex;
			} finally {
				if (tracer != null) {
					Tracer.Info info = new Tracer.Info();
					info.catalog = "database";
					info.name = "query";
                    info.put("serviceName", serviceName);
					info.put("statement", queryString);
					info.put("success", success);
					info.put("startTime", beginTime);
					info.put("runningTime", System.currentTimeMillis() - beginTime);
					trace(info);
				}
			}
		}

		public void query(String queryString, DBResultHanlder handler, Object... args) throws Exception {
			long beginTime = System.currentTimeMillis();
			boolean success = true;
			PreparedInfo preparedInfo = prepareSql(queryString, args);
			try (PreparedStatement stmt = conn.prepareStatement(preparedInfo.sql)) {
				bindParameter(stmt, preparedInfo.args, 1);
				try (ResultSet rs = stmt.executeQuery()) {
					if (handler != null)
						handler.handle(rs);
				}
			} catch (Exception ex) {
				success = false;
				throw ex;
			} finally {
				if (tracer != null) {
					Tracer.Info info = new Tracer.Info();
					info.catalog = "database";
					info.name = "query";
                    info.put("serviceName", serviceName);
					info.put("statement", queryString);
					info.put("success", success);
					info.put("startTime", beginTime);
					info.put("runningTime", System.currentTimeMillis() - beginTime);
					trace(info);
				}
			}
		}

		public DBTable queryTable(String queryString,
							 TableAdjuster adjuster,
							 Map<String, Class<?>> udtMapping,
							 Object... args) throws SQLException {
			try (DBCursor cursor = query(queryString, args)) {
				return cursor.getTable(adjuster, udtMapping);
			}
		}

		public DBTable queryTable(String queryString,
							 TableAdjuster adjuster,
							 Object... args) throws SQLException {
			try (DBCursor cursor = query(queryString, args)) {
				return cursor.getTable(adjuster, null);
			}
		}

		public DBTable queryTable(String queryString,
							 Object... args) throws SQLException {
			try (DBCursor cursor = query(queryString, args)) {
				return cursor.getTable(null, null);
			}
		}

		public <T> List<T> queryList(String queryString,
								 Class<T> type,
								 RowTypeAdapter<T> adapter,
								 Map<String, Class<?>> udtMapping,
								 Object... args) throws SQLException, InstantiationException, IllegalAccessException {
			try (DBCursor cursor = query(queryString, args)) {
				return cursor.getList(type, adapter, udtMapping);
			}
		}

		public <T> List<T> queryList(String queryString,
								 Class<T> type,
								 RowTypeAdapter<T> adapter,
								 Object... args) throws SQLException, InstantiationException, IllegalAccessException {
			try (DBCursor cursor = query(queryString, args)) {
				return cursor.getList(type, adapter, null);
			}
		}

		public <T> List<T> queryList(String queryString,
								 Class<T> type,
								 Object... args) throws SQLException, InstantiationException, IllegalAccessException {
			try (DBCursor cursor = query(queryString, args)) {
				return cursor.getList(type, null, null);
			}
		}
		public List<Map<String, Object>> queryList(String queryString,
									 Object... args) throws SQLException {
			try (DBCursor cursor = query(queryString, args)) {
				return cursor.getList();
			}
		}
		public <T> T queryFirst(String queryString,
								Class<T> type,
								Object... args) throws SQLException, InstantiationException, IllegalAccessException {
			try (DBCursor cursor = query(queryString, args)) {
				if (cursor.next()) {
					return cursor.get(type);
				} else
					return null;
			}
		}

		public Map<String, Object> queryFirst(String queryString, Object... args) throws SQLException {
			try (DBCursor cursor = query(queryString, args)) {
				if (cursor.next()) {
					return cursor.get();
				} else
					return null;
			}
		}

		public <T> T invoke(String procName, Class<T> returnType, Object... args)
				throws SQLException {
			return invoke(procName, returnType, null, args);
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T> T invoke(String procName, Class<T> returnType, Map<String, Class<?>> udtMapping,
				Object... args) throws SQLException {
			long beginTime = System.currentTimeMillis();
			boolean success = true;		
			StringBuilder sqlString = new StringBuilder();
            boolean noReturn = returnType.equals(void.class) || returnType.equals(Void.class);
            int offset;
            if (noReturn) {
                sqlString.append("{call ").append(procName).append("(");
                offset = 1;
            } else {
                sqlString.append("{?=call ").append(procName).append("(");
                offset = 2;
            }
            if (args != null) {
				for (int i = 0; i < args.length; i++) {
					if (i == 0)
						sqlString.append("?");
					else
						sqlString.append(",?");
				}
			}
			sqlString.append(")}");
			try (CallableStatement stmt = conn.prepareCall(sqlString.toString())){
                bindParameter(stmt, args, offset);
                if (!noReturn) {
                    stmt.registerOutParameter(1, toSqlType(returnType));
                }
				stmt.execute();
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						if (args[i] instanceof DBOut) {
							DBOut param = (DBOut)args[i];
							param.setValue(stmtGet(stmt, param.getType(), i + offset, udtMapping));
						}
					}
				}
                if (noReturn)
                    return null;
                else
				    return stmtGet(stmt, returnType, 1, udtMapping);
			} catch (Exception ex) {
				success = false;
				throw ex;
			} finally {
				if (tracer != null) {
					Tracer.Info info = new Tracer.Info();
					info.catalog = "database";
					info.name = "invoke";
                    info.put("serviceName", serviceName);
					info.put("statement", sqlString.toString());
					info.put("success", success);
					info.put("startTime", beginTime);
					info.put("runningTime", System.currentTimeMillis() - beginTime);
					trace(info);
				}
			}
		}
		public void invoke(String procName, Object... args) throws SQLException {
            invoke(procName, void.class, null, args);
		}
		public void invoke(String procName, Map<String, Class<?>> udtMapping, Object... args)
				throws SQLException {
            invoke(procName, void.class, udtMapping, args);
		}
	}

	public class DBRootSession extends DBSession {
		public DBRootSession(Connection conn) throws SQLException {
			super(conn, setting.trace ? DBService.this.tracer : null, DBService.this.logger);
		}
		@Override
		public void close()	{
			super.close();
			Stack<Connection> connections = threadLocal.get();
			if (connections != null) {
				connections.remove(conn);
			}
		}
	}

	public class DBWeakSession extends DBRootSession {
		protected final boolean previousAutoCommit;
		public DBWeakSession(Connection conn) throws SQLException {
			super(conn);
			this.previousAutoCommit = conn.getAutoCommit();
		}
		@Override
		public void commit() throws SQLException {
		}
		@Override
		public void rollback() throws SQLException {
			conn.rollback();
		}
		@Override
		public void close()	{
			try {
				if (!conn.getAutoCommit()) {
					try {
						conn.rollback();
					} catch (SQLException ex) {
						ex.printStackTrace();
					}
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void doWork(DBWork work) throws Exception {
		if (work != null) {
			try (DBSession session = getSession()) {
				work.doWork(session);
			}
		}
	}

	/**
	 * Obtain a DB session, if there has existing session opened in same thread then get it reference.
	 * @return  DB session
	 * @throws SQLException Raise exception when cannot obtain a DB connection.
	 */
    public DBSession getSession() throws SQLException {
        return getSession(true);
    }

	/**
	 * Obtain a DB session, if there has existing session opened in same thread then get it reference.
	 * @param autoCommit Whether or not commit the transaction after statement execute.
	 * @return DB session
	 * @throws SQLException Raise exception when cannot obtain a DB connection.
	 */
	public DBSession getSession(boolean autoCommit) throws SQLException {
		Stack<Connection> connections = threadLocal.get();
		if (connections == null) {
			connections = new Stack<>();
			threadLocal.set(connections);
		}
		if (connections.empty()) {
			Connection connection = getConnection();
			connections.push(connection);
			DBSession dbSession = new DBRootSession(connection);
			dbSession.setAutoCommit(autoCommit);
			return dbSession;
		} else {
			Connection connection = connections.peek();
			DBSession dbSession = new DBWeakSession(connection);
			dbSession.setAutoCommit(autoCommit);
			return dbSession;
		}
	}

	/**
	 * Allocate a new DB session, regardless of whether the current thread has existing session opened.
	 * @param autoCommit Whether or not commit the transaction after statement execute.
	 * @return DB session
	 * @throws SQLException Raise exception when cannot obtain a DB connection.
	 */
	public DBSession getNewSession(boolean autoCommit) throws SQLException {
		Stack<Connection> connections = threadLocal.get();
		if (connections == null) {
			connections = new Stack<>();
			threadLocal.set(connections);
		}
		Connection connection = getConnection();
		connections.push(connection);
		DBSession dbSession = new DBRootSession(connection);
		dbSession.setAutoCommit(autoCommit);
		return dbSession;
	}

	/**
	 * Allocate a new DB connection from the pool,
	 * use raw connection will lost the advanced feature which the DBService provided.<br>
	 * WARNING: should not use this method unless you know exactly what you are doing.
	 * @return DB connection
	 * @throws SQLException Raise exception when cannot obtain a DB connection.
	 */
	public Connection getConnection() throws SQLException {
        if (boneCPConfig == null)
            throw new SQLException("DBService is not started!");
        if (boneCP == null) {
            synchronized (this) {
                if (boneCP == null) {
                    if (lastInitTime != null && System.currentTimeMillis() < lastInitTime + 1000) {
                        // If last try failed in 1 second then throw exception directly.
                        throw new SQLException("Cannot connect to database.");
                    }
                    try {
                        init();
                    } finally {
                        lastInitTime = System.currentTimeMillis();
                    }
                }
            }
        }
        return boneCP.getConnection();
	}

	@SuppressWarnings("unchecked")
	public <T> T getProxy(Class<T> interfaceType) {
		Object instance = Proxy.newProxyInstance(
				DBProxy.class.getClassLoader(),
				new Class<?>[]{interfaceType},
				new DBProxy(this));
		return (T)instance;
	}

    // convenient methods

    public int execute(String queryString, Object... args) throws SQLException {
        try (DBSession session = getSession()) {
            return session.execute(queryString, args);
        }
    }

    public DBTable queryTable(String queryString,
                         TableAdjuster adjuster,
                         Map<String, Class<?>> udtMapping,
                         Object... args) throws SQLException {
        try (DBSession session = getSession()) {
			return session.queryTable(queryString, adjuster, udtMapping, args);
        }
    }

    public DBTable queryTable(String queryString,
                         TableAdjuster adjuster,
                         Object... args) throws SQLException {
        try (DBSession session = getSession()) {
			return session.queryTable(queryString, adjuster, args);
        }
    }

    public DBTable queryTable(String queryString,
                         Object... args) throws SQLException {
        try (DBSession session = getSession()) {
			return session.queryTable(queryString, args);
        }
    }

    public <T> List<T> queryList(String queryString,
                             Class<T> type,
                             RowTypeAdapter<T> adapter,
                             Map<String, Class<?>> udtMapping,
                             Object... args) throws SQLException, InstantiationException, IllegalAccessException {
        try (DBSession session = getSession()) {
			return session.queryList(queryString, type, adapter, udtMapping, args);
        }
    }

    public <T> List<T> queryList(String queryString,
                             Class<T> type,
                             RowTypeAdapter<T> adapter,
                             Object... args) throws SQLException, InstantiationException, IllegalAccessException {
        try (DBSession session = getSession()) {
			return session.queryList(queryString, type, adapter, args);
        }
    }

    public <T> List<T> queryList(String queryString,
                             Class<T> type,
                             Object... args) throws SQLException, InstantiationException, IllegalAccessException {
        try (DBSession session = getSession()) {
			return session.queryList(queryString, type, args);
        }
    }

	public List<Map<String, Object>> queryList(String queryString,
								 Object... args) throws SQLException {
		try (DBSession session = getSession()) {
			return session.queryList(queryString, args);
		}
	}

	public <T> T queryFirst(String queryString,
							 Class<T> type,
							 Object... args) throws SQLException, InstantiationException, IllegalAccessException {
		try (DBSession session = getSession()) {
			return session.queryFirst(queryString, type, args);
		}
	}

	public Map<String, Object> queryFirst(String queryString,
							Object... args) throws SQLException {
		try (DBSession session = getSession()) {
			return session.queryFirst(queryString, args);
		}
	}

    public <T> T invoke(String procName, Class<T> returnType, Object... args)
            throws SQLException {
        try (DBSession session = getSession()) {
            return session.invoke(procName, returnType, null, args);
        }
    }

    public <T> T invoke(String procName, Class<T> returnType, Map<String, Class<?>> udtMapping,
                        Object... args) throws SQLException {
        try (DBSession session = getSession()) {
            return session.invoke(procName, returnType, udtMapping, args);
        }
    }
    public void invoke(String procName, Object... args) throws SQLException {
        try (DBSession session = getSession()) {
            session.invoke(procName, void.class, null, args);
        }
    }

    public void invoke(String procName, Map<String, Class<?>> udtMapping, Object... args)
            throws SQLException {
        try (DBSession session = getSession()) {
            session.invoke(procName, void.class, udtMapping, args);
        }
    }
}