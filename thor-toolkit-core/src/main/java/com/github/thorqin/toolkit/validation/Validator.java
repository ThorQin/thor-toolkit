/*
 * The MIT License
 *
 * Copyright 2014 nuo.qin.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.thorqin.toolkit.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import javassist.Modifier;
import com.github.thorqin.toolkit.validation.annotation.*;

/**
 *
 * @author nuo.qin
 */
public final class Validator {
	private static interface DoubleConvert {
		public Double convert(Object val); 
	}
	private final Stack<String> pathStack = new Stack<>();
	private final static Map<Class<?>, DoubleConvert> numberClass = new HashMap<>();
	
	static {
		numberClass.put(byte.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(byte)val;
			}
		});
		numberClass.put(Byte.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(byte)val; 
			}
		});
		numberClass.put(short.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(short)val;
			}
		});
		numberClass.put(Short.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(short)val;
			}
		});
		numberClass.put(int.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(int)val;
			}
		});
		numberClass.put(Integer.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(int)val;
			}
		});
		numberClass.put(long.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(long)val;
			}
		});
		numberClass.put(Long.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(long)val;
			}
		});
		numberClass.put(float.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(float)val;
			}
		});
		numberClass.put(Float.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(float)val;
			}
		});
		numberClass.put(double.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)val;
			}
		});
		numberClass.put(Double.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)val;
			}
		});
		numberClass.put(char.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(char)val;
			}
		});
		numberClass.put(Character.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return (double)(char)val;
			}
		});
		numberClass.put(BigDecimal.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return ((BigDecimal)val).doubleValue();
			}
		});
		numberClass.put(BigInteger.class, new DoubleConvert(){
			@Override
			public Double convert(Object val) {
				return ((BigInteger)val).doubleValue();
			}
		});
	}
	
	private void validateNumber(Double value, ValidateNumber anno) throws ValidateException {
		if (value == null) {
			if (!anno.allowNull())
				throw new ValidateException("value cannot be null!");
		} else {
			if (value < anno.min() || value > anno.max())
				throw new ValidateException("value range should between " + anno.min() + " and " + anno.max() + "!");
			if (anno.value().length > 0) {
				for (double v : anno.value()) {
					if (Objects.equals(value, v))
						return;
				}
				throw new ValidateException("value isn't in allow list!");
			}
		}
	}
	private void validateBoolean(Boolean value, ValidateBoolean anno) throws ValidateException {
		if (value == null) {
			if (!anno.allowNull())
				throw new ValidateException("value cannot be null!");
		} else {
			if (anno.value().length > 0) {
				for (boolean v : anno.value()) {
					if (Objects.equals(value, v))
						return;
				}
				throw new ValidateException("value isn't in allow list!");
			}
		}
	}
	private void validateString(String value, ValidateString anno) throws ValidateException {
		if (value == null) {
			if (!anno.allowNull())
				throw new ValidateException("String cannot be null!");
		} else {
			if (value.isEmpty()) {
				if (anno.allowEmpty())
					return;
				else
					throw new ValidateException("String cannot be empty!");
			}
			if (value.length() < anno.minLength()|| value.length() > anno.maxLength())
				throw new ValidateException("String length should between " + anno.minLength() + " and " + anno.maxLength() + "!");
			if (!anno.value().trim().isEmpty()) {
				boolean matches = value.matches(anno.value().trim());
				if (!matches) {
					throw new ValidateException("String does not match the given pattern!");
				}
			}
		}
	}
	
	private Date parseDate(String dateStr) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date result;
		try {
			result = formatter.parse(dateStr);
		} catch (ParseException ex) {
			formatter = new SimpleDateFormat("yyyy-MM-dd");
			result = formatter.parse(dateStr);
		}
		return result;
	}
	
	private void validateDate(Date value, ValidateDate anno) throws ValidateException {
		if (value == null) {
			if (!anno.allowNull())
				throw new ValidateException("Date cannot be null!");
		} else {
			try {
			if ((!anno.min().isEmpty() && value.getTime() < parseDate(anno.min()).getTime()) || 
					(!anno.max().isEmpty() && value.getTime() > parseDate(anno.max()).getTime()))
				throw new ValidateException("Date value range should between " + anno.min() + " and " + anno.max() + "!");
			} catch (ParseException ex) {
				throw new ValidateException("Specify invalid date value for min/max value", ex);
			}
		}
	}
	
	private void validateObject(Object value, Class<?> type) throws ValidateException {
		if (value != null) {
			Set<Field> fieldsToCheck = new HashSet<>();
			fieldsToCheck.addAll(Arrays.asList(type.getFields()));
			fieldsToCheck.addAll(Arrays.asList(type.getDeclaredFields()));
			for (Field field : fieldsToCheck) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				String fieldName = field.getName();
				pathStack.push("Validate field: " + fieldName);
				field.setAccessible(true);
				try {
					validateInternal(field.get(value), field.getType(), field.getAnnotations());
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					throw new ValidateException("Cannot access field!", ex);
				}
				if (!pathStack.empty())
					pathStack.pop();
			}
			if (Verifiable.class.isAssignableFrom(type)) {
				Verifiable verifiable = (Verifiable)value;
				verifiable.validate();
			}
		}
	}
	
	private void validateObject(Object value, Validate anno) throws ValidateException {
		if (value == null) {
			if (!anno.allowNull())
				throw new ValidateException("Object cannot be null!");
		} else {
			Class<?> type = value.getClass();
			validateObject(value, type);
		}
	}
	
	private void validateCollectionItem(Object item, ValidateCollection anno) throws ValidateException {
		if (item == null) {
			if (!anno.allowNullItem())
				throw new ValidateException("Item cannot be null!");
		} else {
			Class<?> itemType = anno.itemType();
			if (itemType.isAnnotationPresent(CollectionItemAgent.class)) {
				try {
					Field field = itemType.getField("item");
					itemType = field.getType();
					if (!itemType.isInstance(item))
						throw new ValidateException("Unexpected item type, need: '" + itemType.getName()
						+ "' but found: '" + item.getClass().getName() + "'");
					validateInternal(item, itemType, field.getAnnotations());
				} catch (NoSuchFieldException ex) {
					throw new ValidateException("Unspecified item type in class '" +
							itemType.getName() + "', must provider 'item' field to specify item type and provide annotations.");
				}
			} else {
				if (!itemType.isInstance(item))
					throw new ValidateException("Unexpected item type, need: '" + itemType.getName()
						+ "' but found: '" + item.getClass().getName() + "'");
				validateObject(item, itemType);
			}
		}
	}
	
	private void validateCollection(Collection<?> value, ValidateCollection anno) throws ValidateException {
		if (value == null) {
			if (!anno.allowNull())
				throw new ValidateException("Object cannot be null!");
		} else {
			if (value.size() < anno.minSize() || value.size() > anno.maxSize())
				throw new ValidateException("Items count should between " + anno.minSize() + " and " + anno.maxSize() + "!");
			int i = 0;
			for (Object item : value) {
				pathStack.push("Collection Item: " + i);
				validateCollectionItem(item, anno);
				if (!pathStack.empty())
					pathStack.pop();
				i++;
			}
		}
	}
	
	private void validateArray(Object value, ValidateCollection anno) throws ValidateException {
		if (value == null) {
			if (!anno.allowNull())
				throw new ValidateException("Object cannot be null!");
		} else {
			int len = Array.getLength(value);
			if (len < anno.minSize() || len > anno.maxSize())
				throw new ValidateException("Items count should between " + anno.minSize() + " and " + anno.maxSize() + "!");
			for (int i = 0; i < len; i++) {
				pathStack.push("Array Item: " + i);
				Object item = Array.get(value, i);
				validateCollectionItem(item, anno);
				if (!pathStack.empty())
					pathStack.pop();
			}
		}
	}
	
	private void validateMap(Map<?,?> value, ValidateMap anno) throws ValidateException {
		if (value == null) {
			if (!anno.allowNull())
				throw new ValidateException("Object cannot be null!");
		} else {
			Class<?> type = anno.type();
			Set<Field> fieldsToCheck = new HashSet<>();
			fieldsToCheck.addAll(Arrays.asList(type.getFields()));
			fieldsToCheck.addAll(Arrays.asList(type.getDeclaredFields()));
			for (Field field : fieldsToCheck) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				String key = field.getName();
				pathStack.push("Validate field: " + key);
				Object obj = value.get(key);
				validateInternal(obj, field.getType(), field.getAnnotations());
				if (!pathStack.empty())
					pathStack.pop();
			}
		}
	}
	
	private static Double toDouble(Object obj, Class<?> type) {
		if (obj == null)
			return null;
		return numberClass.get(type).convert(obj);
	}
	private static Boolean toBoolean(Object obj) {
		if (obj == null)
			return null;
		else if (obj.getClass().equals(boolean.class))
			return (boolean)obj;
		else if (obj.getClass().equals(Boolean.class))
			return (Boolean)obj;
		else
			return null;
	}
	
	private static boolean isDate(Class<?> type) {
		return type.equals(Date.class);
	}
	private static boolean isBoolean(Class<?> type) {
		return type.equals(boolean.class) || type.equals(Boolean.class);
	}
	private static boolean isNumber(Class<?> type) {
		return numberClass.containsKey(type);
	}
	private static boolean isCollection(Class<?> type) {
		return Collection.class.isAssignableFrom(type);
	}
	private static boolean isArray(Class<?> type) {
		return type.isArray();
	}
	private static boolean isMap(Class<?> type) {
		return Map.class.isAssignableFrom(type);
	}
	private static boolean isString(Class<?> type) {
		return type.equals(String.class);
	}
	
	private static boolean isDateOrNull(Object value) {
		if (value == null)
			return true;
		else
			return (Date.class.isInstance(value));
	}
	private static boolean isStringOrNull(Object value) {
		if (value == null)
			return true;
		else 
			return (String.class.isInstance(value));
	}
	private static boolean isNumberOrNull(Object value) {
		if (value == null)
			return true;
		else
			return isNumber(value.getClass());
	}
	private static boolean isBooleanOrNull(Object value) {
		if (value == null)
			return true;
		else
			return isBoolean(value.getClass());
	}
	private static boolean isCollectionOrNull(Object value) {
		if (value == null)
			return true;
		else
			return Collection.class.isInstance(value);
	}
	private static boolean isArrayOrNull(Object value) {
		if (value == null)
			return true;
		else
			return value.getClass().isArray();
	}
	private static boolean isMapOrNull(Object value) {
		if (value == null)
			return true;
		else
			return Map.class.isInstance(value);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getAnnotation(Annotation[] annotations, Class<T> annoType) throws ValidateException {
		boolean hasOtherType = false;
		for (Annotation anno : annotations) {
			if (annoType.isInstance(anno))
				return (T)anno;
			else if (Validate.class.isInstance(anno) || 
					ValidateString.class.isInstance(anno) ||
					ValidateNumber.class.isInstance(anno) || 
					ValidateCollection.class.isInstance(anno) ||
					ValidateMap.class.isInstance(anno) ) {
				hasOtherType = true;
			}
		}
		if (!hasOtherType)
			return null;
		else
			throw new ValidateException("Unexpected validation rule, given rule cannot match the object type!");
	}
	
	private void validateInternal(Object object, Class<?> type, Annotation[] annotations) throws ValidateException {
		if (isString(type)) {
			if (!isStringOrNull(object))
				throw new ValidateException("Invalid object type, need: '" + type.getName() + 
					"', but found: '" + object.getClass().getName() + "'.");
			ValidateString anno = getAnnotation(annotations, ValidateString.class);
			if (anno != null)
				validateString((String)object, anno);
		} else if (isNumber(type)) {
			if (!isNumberOrNull(object))
				throw new ValidateException("Invalid object type, need: '" + type.getName() + 
					"', but found: '" + object.getClass().getName() + "'.");
			ValidateNumber anno = getAnnotation(annotations, ValidateNumber.class);
			if (anno != null)
				validateNumber(toDouble(object, type), anno);
		} else if (isBoolean(type)) {
			if (!isBooleanOrNull(object))
				throw new ValidateException("Invalid object type, need: '" + type.getName() + 
					"', but found: '" + object.getClass().getName() + "'.");
			ValidateBoolean anno = getAnnotation(annotations, ValidateBoolean.class);
			if (anno != null)
				validateBoolean(toBoolean(object), anno);
		} else if (isDate(type)) {
			if (!isDateOrNull(object))
				throw new ValidateException("Invalid object type, need: '" + type.getName() + 
					"', but found: '" + object.getClass().getName() + "'.");
			ValidateDate anno = getAnnotation(annotations, ValidateDate.class);
			if (anno != null)
				validateDate((Date)object, anno);
		} else if (isCollection(type)) {
			if (!isCollectionOrNull(object))
				throw new ValidateException("Invalid object type, need: '" + type.getName() + 
					"', but found: '" + object.getClass().getName() + "'.");
			ValidateCollection anno = getAnnotation(annotations, ValidateCollection.class);
			if (anno != null)
				validateCollection((Collection<?>)object, anno);
		} else if (isArray(type)) {
			if (!isArrayOrNull(object))
				throw new ValidateException("Invalid object type, need: '" + type.getName() + 
					"', but found: '" + object.getClass().getName() + "'.");
			ValidateCollection anno = getAnnotation(annotations, ValidateCollection.class);
			if (anno != null)
				validateArray(object, anno);
		} else if (isMap(type)) {
			if (!isMapOrNull(object))
				throw new ValidateException("Invalid object type, need: '" + type.getName() + 
					"', but found: '" + object.getClass().getName() + "'.");
			ValidateMap anno = getAnnotation(annotations, ValidateMap.class);
			if (anno != null)
				validateMap((Map<?,?>)object, anno);
		} else {
			if (object != null && !type.isInstance(object))
				throw new ValidateException("Invalid object type, need: '" + type.getName() + 
						"', but found: '" + object.getClass().getName() + "'.");
			Validate anno = getAnnotation(annotations, Validate.class);
			if (anno != null)
				validateObject(object, anno);
		}
	}
	
	public void validate(Object object, Class<?> type, Annotation[] annotations) throws ValidateException {
		try {
			pathStack.clear();
			validateInternal(object, type, annotations);
		} catch (Exception ex) {
			StringBuilder sb = new StringBuilder();
			sb.append("ERROR: Validate failed!!! Cause by: ");
			sb.append(ex.getMessage());
			while (!pathStack.empty()) {
				sb.append("\n\tat * ");
				sb.append(pathStack.pop());
			}
			throw new ValidateException(sb.toString());
		}
	}
	
	public void validateObject(Object object, Class<?> type, final boolean allowNull) throws ValidateException {
		validate(object, type, new Annotation[]{
			new Validate() {
				@Override
				public boolean allowNull() {
					return allowNull;
				}

				@Override
				public Class<? extends Annotation> annotationType() {
					return Validate.class;
				}
			}
		});
	}
}
