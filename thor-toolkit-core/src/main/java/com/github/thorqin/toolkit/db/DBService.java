package com.github.thorqin.toolkit.db;

import com.github.thorqin.toolkit.log.*;
import com.github.thorqin.toolkit.validation.ValidateException;
import com.github.thorqin.toolkit.validation.Validator;
import com.github.thorqin.toolkit.validation.annotation.ValidateNumber;
import com.github.thorqin.toolkit.validation.annotation.ValidateString;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Struct;
import java.util.*;
import java.util.logging.Level;

/**********************************************************
 * DBStore implementation
 * @author nuo.qin
 *
 **********************************************************/
public class DBService {
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
	}
	private static final java.util.logging.Logger logger =
			java.util.logging.Logger.getLogger(DBService.class.getName());
	private static final Map<Class<?>, Integer> typeMapping;
	// This type very depends on which database are you using.
	private static final Map<Class<?>, String> arrayType;
	
	static {
		typeMapping = new HashMap<>();
		typeMapping.put(List.class, java.sql.Types.ARRAY);
		typeMapping.put(byte[].class, java.sql.Types.VARBINARY);
		typeMapping.put(Void.class, java.sql.Types.JAVA_OBJECT);
		typeMapping.put(void.class, java.sql.Types.JAVA_OBJECT);
		typeMapping.put(Date.class, java.sql.Types.TIMESTAMP);
		typeMapping.put(Calendar.class, java.sql.Types.TIMESTAMP);
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
	}
	
	private static int toSqlType(Class<?> type) {
		Integer sqlType = typeMapping.get(type);
		if (sqlType == null) {
			if (type.isArray())
				return java.sql.Types.ARRAY; 
			else if (type.isAnnotationPresent(UDT.class))
				return java.sql.Types.STRUCT;
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
	
	private static Date fromSqlDate(java.sql.Time time) {
		return new Date(time.getTime());
	}
	private static Date fromSqlDate(java.sql.Date time) {
		return new Date(time.getTime());
	}
	private static Date fromSqlDate(java.sql.Timestamp time) {
		return new Date(time.getTime());
	}

	private static Object fromSqlObject(Object obj, Class<?> destType, Map<String, Class<?>> udtMapping) throws SQLException {
		if (obj == null)
			return null;
		Class<?> type = obj.getClass();
		if (destType.equals(Date.class) && type.equals(java.sql.Timestamp.class)) {
			return fromSqlDate((java.sql.Timestamp)obj);
		} else if (destType.equals(Date.class) && type.equals(java.sql.Date.class)) {
			return fromSqlDate((java.sql.Date)obj);
		} else if (destType.equals(Date.class) && type.equals(java.sql.Time.class)) {
			return fromSqlDate((java.sql.Time)obj);
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
			Object instance = null;
			try {
				instance = destType.newInstance();
			} catch (InstantiationException
					| IllegalAccessException e) {
				throw new SQLException("Parse java.sql.Struct failed.", e);
			}
			Field[] fields = destType.getFields();
			for (int i = 0, j = 0; i < fields.length && j < attributes.length; i++) {
				if (!fields[i].isAnnotationPresent(UDTSkip.class)) {
					try {
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

	private static Object fromSqlObject(Object obj, Map<String, Class<?>> udtMapping) throws SQLException {
		if (obj == null)
			return null;
		Class<?> type = obj.getClass();
		if (type.equals(java.sql.Timestamp.class)) {
			return fromSqlDate((java.sql.Timestamp)obj);
		} else if (type.equals(java.sql.Date.class)) {
			return fromSqlDate((java.sql.Date)obj);
		} else if (type.equals(java.sql.Time.class)) {
			return fromSqlDate((java.sql.Time)obj);
		} else if (obj instanceof ResultSet) {
			return new DBCursor((ResultSet)obj);
		} else if (obj instanceof Array) {
			Array array = (Array)obj;
			List<Object> list = new LinkedList<>();
			try (ResultSet arrayResult = array.getResultSet()) {
				while (arrayResult.next()) {
					list.add(fromSqlObject(arrayResult.getObject(2), udtMapping));
				}
				return list;
			} 
		} else if (obj instanceof Struct) {
			Struct struct = (Struct)obj;
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
					if (!fields[i].isAnnotationPresent(UDTSkip.class)) {
						try {
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
			return obj;
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
		public void setValue(Object[] row, String column, Object value) {
			buildHeadMapping();
			Integer pos = headMapping.get(column);
			if (pos != null) {
				row[pos] = value;
			} else
				throw new InvalidParameterException("Column '" + column + "' doesn't exist!");
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface UDT {
		String udtName() default "";
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface UDTSkip {
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

	public static interface DBResultHanlder {
		void handle(ResultSet result) throws Exception;
	}
	
	public static class DBCursor implements AutoCloseable {
		private ResultSet resultSet;
		private String[] columns = new String[0];

		private String[] buildColumnMap() throws SQLException {
			if (resultSet == null) {
				return new String[0];
			}
			ResultSetMetaData mataData = resultSet.getMetaData();
			int columnCount = resultSet.getMetaData().getColumnCount();
			String[] columns = new String[columnCount];
			for (int i = 1; i <= columnCount; i++) {
				columns[i - 1] = mataData.getColumnLabel(i);
			}
			return columns;
		}

		public DBCursor() {
			resultSet = null;
		}
		public DBCursor(ResultSet resultSet) throws SQLException {
			this.resultSet = resultSet;
			columns = buildColumnMap();
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
		public <T> T getValue(int column) throws SQLException {
			return (T)fromSqlObject(resultSet.getObject(column), null);
		}
		public <T> T getValue(int column, Map<String, Class<?>> udtMapping) throws SQLException {
			return (T)fromSqlObject(resultSet.getObject(column), udtMapping);
		}
		public <T> T getValue(String columnName) throws SQLException {
			return (T)getValue(columnName, null);
		}
		public <T> T getValue(String columnName, Map<String, Class<?>> udtMapping) throws SQLException {
			Integer idx = null;
			for (int i = 0; i < columns.length; i++) {
				if (columns[i].equalsIgnoreCase(columnName))
					idx = i + 1;
			}
			if (idx == null)
				return null;
			return (T)fromSqlObject(resultSet.getObject(idx), udtMapping);
		}

		public ResultSet getResultSet() {
			return resultSet;
		}
		public void setResultSet(ResultSet resultSet) throws SQLException {
			this.resultSet = resultSet;
			columns = buildColumnMap();
		}
		public void perform(DBResultHanlder handler) throws Exception {
			if (resultSet != null)
				handler.handle(resultSet);
		}
		public <T> List<T> getList(Class<T> type) throws IllegalAccessException, SQLException, InstantiationException {
			return getList(type, null);
		}

		public <T> List<T> getList(Class<T> type, Map<String, Class<?>> udtMapping) throws IllegalAccessException, InstantiationException, SQLException {
			List<T> list = new LinkedList<>();
			if (resultSet == null)
				return list;
			while (resultSet.next()) {
				T obj = type.newInstance();
				for (Field field : type.getDeclaredFields()) {
					DBField anno = field.getAnnotation(DBField.class);
					if (anno == null)
						continue;
					String colName = anno.value();
					if (colName.isEmpty())
						colName = field.getName();
					Integer col = null;
					for (int i = 0; i < columns.length; i++) {
						if (columns[i].equalsIgnoreCase(colName)) {
							col = i + 1;
							break;
						}
					}
					if (col != null) {
						field.set(obj, fromSqlObject(resultSet.getObject(col), udtMapping));
					}
				}
				list.add(obj);
			}
			return list;
		}
		public DBTable getTable() throws SQLException {
			return getTable(null);
		}
		public DBTable getTable(Map<String, Class<?>> udtMapping) throws SQLException {
			if (resultSet == null)
				return null;
			DBTable table = new DBTable();
			table.head = Arrays.copyOf(columns, columns.length);
			LinkedList<Object[]> list = new LinkedList<>();
			while (resultSet.next()) {
				Object[] line = new Object[table.head.length];
				for (int i = 1; i <= table.head.length; i++) {
					line[i - 1] = fromSqlObject(resultSet.getObject(i), udtMapping);
				}
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
		}
	}
	

	public static interface DBWork {
		public void doWork(DBSession session) throws Exception;
	}
	public static interface DBProxyWork<T> {
		public void doWork(T proxy) throws Exception;
	}

	/* Private properties. */
	//final private BoneCPDataSource boneCP;
	final private BoneCP boneCP;
	final private DBSetting setting;
	final private Set<Logger> loggerSet = new HashSet<>();

	public synchronized void addLogger(Logger logger) {
		loggerSet.add(logger);
	}

	public synchronized void removeLogger(Logger logger) {
		loggerSet.remove(logger);
	}
	private synchronized void log(Logger.LogInfo info) {
		for (Logger lg: loggerSet) {
			try {
				lg.log(info);
			} catch (Exception ex) {
				DBService.logger.log(Level.WARNING, "Record log failed.", ex);
			}
		}
	}
	
	public DBService(DBSetting dbSetting) throws ValidateException {
		Validator validator = new Validator();
		validator.validateObject(dbSetting, DBSetting.class, false);
		this.setting = dbSetting;
		try {
			Class.forName(setting.driver);
			BoneCPConfig boneCPConfig = new BoneCPConfig();
			//boneCP = new BoneCPDataSource();
			//boneCP.setDriverClass(setting.driver);
			
			boneCPConfig.setDefaultAutoCommit(true);
			boneCPConfig.setJdbcUrl(setting.uri);
			boneCPConfig.setJdbcUrl(setting.uri);
			boneCPConfig.setUsername(setting.user);
			boneCPConfig.setPassword(setting.password);
			boneCPConfig.setMinConnectionsPerPartition(setting.minConnectionsPerPartition);
			boneCPConfig.setMaxConnectionsPerPartition(setting.maxConnectionsPerPartition);
			boneCPConfig.setPartitionCount(setting.partitionCount);
			boneCP = new BoneCP(boneCPConfig);
		} catch (Exception ex) {
			throw new ServiceConfigurationError("Initialize DB Service failed.", ex);
		}
	}

	public void close() {
		try {
			boneCP.close();
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Shutdown boneCP failed!", ex);
		}
	}
	
	public class DBSession implements AutoCloseable {
		private final Connection conn;
		public DBSession(Connection conn) throws SQLException {
			this.conn = conn;
			this.conn.setAutoCommit(true);
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
		public void close() throws SQLException	{
			conn.close();
		}

		@SuppressWarnings("unchecked")
		public <T> T getProxy(Class<T> interfaceType, boolean autoCommit) throws SQLException {
			Object instance = Proxy.newProxyInstance(
					DBProxy.class.getClassLoader(),
					new Class<?>[]{interfaceType},
					new DBProxy(this, autoCommit));
			return (T)instance;
		}

		private Struct toUdt(Object obj) throws SQLException {
			if (obj == null)
				return null;
			Class<?> clazz = obj.getClass();
			UDT udt = clazz.getAnnotation(UDT.class);
			if (udt == null) {
				return null;
			}
			ArrayList<Object> attributes = new ArrayList<>(clazz.getFields().length);
			for (Field field : clazz.getFields()) {
				if (!field.isAnnotationPresent(UDTSkip.class)) {
					try {
						attributes.add(toSqlObj(field.get(obj)));
					} catch (IllegalArgumentException
							| IllegalAccessException e) {
						throw new SQLException("Convert object to java.sql.Struct failed.", e);
					}
				}
			}
			return conn.createStruct(udt.udtName(), attributes.toArray());
		}
		
		private Object toSqlObj(Object obj) throws SQLException {
			Struct udt;
			if (obj == null) {
				return null;
			} else if (obj.getClass().equals(Date.class)) {
				return toSqlDate((Date)obj);
			} else if (obj.getClass().equals(Calendar.class)) {
				return toSqlDate((Calendar)obj);
			} else if (obj.getClass().equals(DBCursor.class)) {
				return ((DBCursor)obj).getResultSet(); 
			} else if ((udt = toUdt(obj)) != null) {
				return udt;
			} else {
				return obj;
			}
		}

		@SuppressWarnings("rawtypes")
		private void bindParameter(PreparedStatement stmt, Object[] args, int offset) throws SQLException {
			if (args == null)
				return;
			Struct udt;
			for (Object obj : args) {
				if (obj == null) {
					stmt.setNull(offset++, java.sql.Types.NULL);
				} else {
					Class<?> paramType = obj.getClass();
					if (DBRef.class.isAssignableFrom(paramType)) {
						obj = ((DBRef)obj).getValue();
						if (obj == null)
							stmt.setNull(offset++, java.sql.Types.NULL);
						else if (obj.getClass().equals(Date.class)) {
							stmt.setTimestamp(offset++, toSqlDate((Date)obj));
						} else if (obj.getClass().equals(Calendar.class)) {
							stmt.setTimestamp(offset++, toSqlDate((Calendar)obj));
						} else if (obj.getClass().equals(DBCursor.class)) {
							stmt.setObject(offset++, ((DBCursor)obj).getResultSet());
						} else if ((udt = toUdt(obj)) != null) {
							stmt.setObject(offset++, udt, java.sql.Types.STRUCT);
						} else {
							stmt.setObject(offset++, obj);
						}
					} else if (DBOut.class.isAssignableFrom(paramType)) {
						stmt.setNull(offset++, java.sql.Types.NULL);
					} else if (paramType.equals(Date.class)) {
						stmt.setTimestamp(offset++, toSqlDate((Date)obj));
					} else if (paramType.equals(Calendar.class)) {
						stmt.setTimestamp(offset++, toSqlDate((Calendar)obj));
					} else if ((udt = toUdt(obj)) != null) {
						stmt.setObject(offset++, udt, java.sql.Types.STRUCT);
					} else if (paramType.equals(DBCursor.class)) {
						stmt.setObject(offset++, ((DBCursor)obj).getResultSet());
					} else if (paramType.isArray()) {
						Array array = toSqlArray(stmt.getConnection(), (Object[])obj);
						stmt.setArray(offset++, array);
					} else {
						stmt.setObject(offset++, obj);
					}
				}
			}
		}
		
		@SuppressWarnings("rawtypes")
		private void bindParameter(CallableStatement stmt, Object[] args, int offset) throws SQLException {
			if (args == null)
				return;
			Struct udt;
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
						else if (obj.getClass().equals(Date.class)) {
							stmt.setTimestamp(offset++, toSqlDate((Date)obj));
						} else if (obj.getClass().equals(Calendar.class)) {
							stmt.setTimestamp(offset++, toSqlDate((Calendar)obj));
						} else if (obj.getClass().equals(DBCursor.class)) {
							stmt.setObject(offset++, ((DBCursor)obj).getResultSet());
						} else if ((udt = toUdt(obj)) != null) {
							stmt.setObject(offset++, udt, java.sql.Types.STRUCT);
						} else {
							stmt.setObject(offset++, obj);
						}
					} else if (DBOut.class.isAssignableFrom(paramType)) {
						stmt.registerOutParameter(offset++, toSqlType(((DBOut)obj).getType()));
					} else if (paramType.equals(Date.class)) {
						stmt.setTimestamp(offset++, toSqlDate((Date)obj));
					} else if (paramType.equals(Calendar.class)) {
						stmt.setTimestamp(offset++, toSqlDate((Calendar)obj));
					} else if (paramType.equals(DBCursor.class)) {
						stmt.setObject(offset++, ((DBCursor)obj).getResultSet());
					} else if ((udt = toUdt(obj)) != null) {
						stmt.setObject(offset++, udt, java.sql.Types.STRUCT);
					}  else if (paramType.isArray()) {
						Array array = toSqlArray(stmt.getConnection(), (Object[])obj);
						stmt.setArray(offset++, array);
					} else {
						stmt.setObject(offset++, obj);
					}
				}
			}
		}

		public int execute(String queryString) throws SQLException {
			return execute(queryString, null);
		}
		public int execute(String queryString, Object[] args) throws SQLException {
			long beginTime = System.currentTimeMillis();
			boolean success = true;
			try (PreparedStatement stmt = conn.prepareStatement(queryString)){
				bindParameter(stmt, args, 1);
				return stmt.executeUpdate();
			} catch (Exception ex) {
				success = false;
				throw ex;
			} finally {
				if (!loggerSet.isEmpty()) {
					Logger.LogInfo info = new Logger.LogInfo();
					info.catalog = "database";
					info.name = "execute";
					info.put("statement", queryString);
					info.put("success", success);
					info.put("startTime", beginTime);
					info.put("runningTime", System.currentTimeMillis() - beginTime);
					log(info);
				}
			}
		}
		public DBCursor query(String queryString) throws SQLException {
			return query(queryString, (Map<String, Class<?>>)null, null);
		}
		
		public DBCursor query(String queryString,
				Object[] args) throws SQLException {
			return query(queryString, (Map<String, Class<?>>)null, args);
		}
		
		public DBCursor query(String queryString,
				Map<String, Class<?>> udtMapping,
				Object[] args) throws SQLException {
			long beginTime = System.currentTimeMillis();
			boolean success = true;
			try (PreparedStatement stmt = conn.prepareStatement(queryString)) {
				bindParameter(stmt, args, 1);
				ResultSet rs = stmt.executeQuery();
				return new DBCursor(rs);
			} catch (Exception ex) {
				success = false;
				throw ex;
			} finally {
				if (!loggerSet.isEmpty()) {
					Logger.LogInfo info = new Logger.LogInfo();
					info.catalog = "database";
					info.name = "query";
					info.put("statement", queryString);
					info.put("success", success);
					info.put("startTime", beginTime);
					info.put("runningTime", System.currentTimeMillis() - beginTime);
					log(info);
				}
			}
		}
		public void query(String queryString, DBResultHanlder handler)
				throws Exception {
			query(queryString, handler, null);
		}

		public void query(String queryString, DBResultHanlder handler, Object[] args) throws Exception {
			long beginTime = System.currentTimeMillis();
			boolean success = true;			
			try (PreparedStatement stmt = conn.prepareStatement(queryString)) {
				bindParameter(stmt, args, 1);
				try (ResultSet rs = stmt.executeQuery()) {
					if (handler != null)
						handler.handle(rs);
				}
			} catch (Exception ex) {
				success = false;
				throw ex;
			} finally {
				if (!loggerSet.isEmpty()) {
					Logger.LogInfo info = new Logger.LogInfo();
					info.catalog = "database";
					info.name = "query";
					info.put("statement", queryString);
					info.put("success", success);
					info.put("startTime", beginTime);
					info.put("runningTime", System.currentTimeMillis() - beginTime);
					log(info);
				}
			}
		}
		public <T> T invoke(String procName, Class<T> returnType) throws SQLException {
			return invoke(procName, returnType, null, null);
		}
		public <T> T invoke(String procName, Class<T> returnType, Object[] args)
				throws SQLException {
			return invoke(procName, returnType, null, args);
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T> T invoke(String procName, Class<T> returnType, Map<String, Class<?>> udtMapping,
				Object[] args) throws SQLException {
			long beginTime = System.currentTimeMillis();
			boolean success = true;		
			StringBuilder sqlString = new StringBuilder();
			sqlString.append("{?=call ").append(procName).append("(");
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
				bindParameter(stmt, args, 2);
				stmt.registerOutParameter(1, toSqlType(returnType));
				stmt.execute();
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						if (args[i] instanceof DBOut) {
							DBOut param = (DBOut)args[i];
							if (param.getType().equals(DBTable.class)) {
								DBCursor cursor = (DBCursor)fromSqlObject(stmt.getObject(i + 2), udtMapping);
								param.setValue(cursor.getTable());
							} else {
								param.setValue(fromSqlObject(stmt.getObject(i + 2), udtMapping));
							}
						}
					}
				}
				if (returnType.equals(DBTable.class)) {
					DBCursor cursor = (DBCursor)fromSqlObject(stmt.getObject(1), udtMapping);
					return (T)cursor.getTable();
				} else
					return (T)fromSqlObject(stmt.getObject(1), udtMapping);
			} catch (Exception ex) {
				success = false;
				throw ex;
			} finally {
				if (!loggerSet.isEmpty()) {
					Logger.LogInfo info = new Logger.LogInfo();
					info.catalog = "database";
					info.name = "invoke";
					info.put("statement", sqlString.toString());
					info.put("success", success);
					info.put("startTime", beginTime);
					info.put("runningTime", System.currentTimeMillis() - beginTime);
					log(info);
				}
			}
		}
		public void perform(String procName) throws SQLException {
			perform(procName, null, null);
		}
		public void perform(String procName, Object[] args) throws SQLException {
			perform(procName, null, args);
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public void perform(String procName, Map<String, Class<?>> udtMapping, Object[] args) 
				throws SQLException {
			long beginTime = System.currentTimeMillis();
			boolean success = true;			
			StringBuilder sqlString = new StringBuilder();
			sqlString.append("{call ").append(procName).append("(");
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
				bindParameter(stmt, args, 1);
				stmt.execute();
				if (args != null) {
					for (int i = 0; i < args.length; i++) {
						if (args[i] instanceof DBOut) {
							DBOut param = (DBOut)args[i];
							if (param.getType().equals(DBTable.class)) {
								DBCursor cursor = (DBCursor)fromSqlObject(stmt.getObject(i + 1), udtMapping);
								param.setValue(cursor.getTable());
							} else {
								param.setValue(fromSqlObject(stmt.getObject(i + 1), udtMapping));
							}
						}
					}
				}
			} catch (Exception ex) {
				success = false;
				throw ex;
			} finally {
				if (!loggerSet.isEmpty()) {
					Logger.LogInfo info = new Logger.LogInfo();
					info.catalog = "database";
					info.name = "perform";
					info.put("statement", sqlString.toString());
					info.put("success", success);
					info.put("startTime", beginTime);
					info.put("runningTime", System.currentTimeMillis() - beginTime);
					log(info);
				}
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
	 * Do a database work by through a serials DBProxy invokes.
	 * @param work Database work
	 * @param interfaceType Proxy interface class
	 * @param autoCommit Whether commit transaction when a proxy method called.
	 *                   if autoCommit is false then transaction will be committed when
	 *                   the work completely finished. if true then will commit transaction
	 *                   immediately when after a method invoked.
	 * @param <T> Proxy interface
	 * @throws Exception Any exception
	 */
	public <T> void doWork(DBProxyWork<T> work, Class<T> interfaceType, boolean autoCommit) throws Exception {
		if (work != null) {
			try (DBSession session = getSession()) {
				T proxy = session.getProxy(interfaceType, autoCommit);
				work.doWork(proxy);
				if (autoCommit == false)
					session.commit();
			}
		}
	}
	public <T> void doWork(DBProxyWork<T> work, Class<T> interfaceType) throws Exception {
		doWork(work, interfaceType, false);
	}
	public DBSession getSession() throws SQLException {
		return new DBSession(getConnection());
	}
	public Connection getConnection() throws SQLException {
		return boneCP.getConnection();
	}
}