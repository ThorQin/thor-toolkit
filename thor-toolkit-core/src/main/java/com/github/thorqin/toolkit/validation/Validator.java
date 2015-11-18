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
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

import com.github.thorqin.toolkit.utility.Localization;
import javassist.Modifier;
import com.github.thorqin.toolkit.validation.annotation.*;
import org.joda.time.DateTime;

/**
 *
 * @author nuo.qin
 */
public final class Validator {
	private interface DoubleConvert {
		Double convert(Object val);
	}
	private final Stack<String> pathStack = new Stack<>();
	private final static Map<Class<?>, DoubleConvert> numberClass = new HashMap<>();
    private final Localization loc;

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

    public Validator(Localization loc) {
        this.loc = loc;
    }

	private void validateNumber(Double value, String name, ValidateNumber anno) throws ValidateException {
        pathStack.push(anno.name().isEmpty() ? name: anno.name());
		if (value == null) {
			if (!anno.allowNull())
				throw new ValidateException(ValidateMessageConstant.CANNOT_BE_NULL.getMessage(loc));
		} else {
            if (anno.min() != Double.MIN_VALUE && anno.max() != Double.MAX_VALUE) {
                if (value < anno.min() || value > anno.max())
                    throw new ValidateException(ValidateMessageConstant.VALUE_SHOULD_BETWEEN.getMessage(loc, anno.min(), anno.max()));
            } else if (anno.min() != Double.MIN_VALUE) {
                if (value < anno.min())
                    throw new ValidateException(ValidateMessageConstant.VALUE_SHOULD_GREAT_THAN.getMessage(loc, anno.min()));
            } else if (anno.max() != Double.MAX_VALUE) {
                if (value > anno.max())
                    throw new ValidateException(ValidateMessageConstant.VALUE_SHOULD_LESS_THAN.getMessage(loc, anno.max()));
            }
			if (anno.value().length > 0) {
				for (double v : anno.value()) {
					if (Objects.equals(value, v)) {
                        pathStack.pop();
                        return;
                    }
				}
                throw new ValidateException(ValidateMessageConstant.INVALID_VALUE.getMessage(loc));
			}
		}
        pathStack.pop();
	}
	private void validateBoolean(Boolean value, String name, ValidateBoolean anno) throws ValidateException {
        pathStack.push(anno.name().isEmpty() ? name: anno.name());
        if (value == null) {
			if (!anno.allowNull())
                throw new ValidateException(ValidateMessageConstant.CANNOT_BE_NULL.getMessage(loc));
		} else {
			if (anno.value().length > 0) {
				for (boolean v : anno.value()) {
					if (Objects.equals(value, v)) {
                        pathStack.pop();
                        return;
                    }
				}
                throw new ValidateException(ValidateMessageConstant.INVALID_VALUE.getMessage(loc));
			}
		}
        pathStack.pop();
	}
	private void validateString(String value, String name, ValidateString anno) throws ValidateException {
        pathStack.push(anno.name().isEmpty() ? name: anno.name());
        if (value == null) {
			if (!anno.allowNull())
                throw new ValidateException(ValidateMessageConstant.CANNOT_BE_NULL.getMessage(loc));
		} else {
			if (value.isEmpty()) {
				if (anno.allowEmpty()) {
                    pathStack.pop();
                    return;
                } else
                    throw new ValidateException(ValidateMessageConstant.CANNOT_BE_EMPTY.getMessage(loc));
			}
            if (anno.minLength() > 0 && anno.maxLength() != Integer.MAX_VALUE) {
                if (value.length() < anno.minLength() || value.length() > anno.maxLength())
                    throw new ValidateException(ValidateMessageConstant.LENGTH_SHOULD_BETWEEN.getMessage(loc, anno.minLength(), anno.maxLength()));
            } else if (anno.minLength() > 0) {
                if (value.length() < anno.minLength())
                    throw new ValidateException(ValidateMessageConstant.LENGTH_SHOULD_GREAT_THAN.getMessage(loc, anno.minLength()));
            } else if (anno.maxLength() != Integer.MAX_VALUE) {
                if (value.length() > anno.maxLength())
                    throw new ValidateException(ValidateMessageConstant.LENGTH_SHOULD_LESS_THAN.getMessage(loc, anno.maxLength()));
            }
			if (!anno.value().trim().isEmpty()) {
				boolean matches = value.matches(anno.value().trim());
				if (!matches) {
                    throw new ValidateException(ValidateMessageConstant.INVALID_FORMAT.getMessage(loc));
				}
			}
		}
        pathStack.pop();
	}

	private void validateDate(DateTime value, String name, ValidateDate anno) throws ValidateException {
        pathStack.push(anno.name().isEmpty() ? name: anno.name());
        if (value == null) {
			if (!anno.allowNull())
                throw new ValidateException(ValidateMessageConstant.CANNOT_BE_NULL.getMessage(loc));
		} else {
            try {
                DateTime minDate = null, maxDate = null;
                if (!anno.min().isEmpty())
                    minDate = DateTime.parse(anno.min());
                if (!anno.max().isEmpty())
                    maxDate = DateTime.parse(anno.max());
                if (minDate != null && maxDate != null) {
                    if (value.getMillis() < minDate.getMillis() ||
                            value.getMillis() > maxDate.getMillis())
                        throw new ValidateException(ValidateMessageConstant.TIME_SHOULD_BETWEEN.getMessage(loc, anno.min(), anno.max()));
                } else if (minDate != null) {
                    if (value.getMillis() < minDate.getMillis())
                        throw new ValidateException(ValidateMessageConstant.TIME_SHOULD_GREAT_THAN.getMessage(loc, anno.min()));
                } else if (maxDate != null) {
                    if (value.getMillis() > maxDate.getMillis())
                        throw new ValidateException(ValidateMessageConstant.TIME_SHOULD_LESS_THAN.getMessage(loc, anno.max()));
                }
            } catch (IllegalArgumentException ex) {
                throw new ValidateException(ValidateMessageConstant.INVALID_TIME_FORMAT.getMessage(loc));
            }
        }
        pathStack.pop();
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
				field.setAccessible(true);
				try {
					validateInternal(field.get(value), field.getType(), fieldName, field.getAnnotations());
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					throw new ValidateException("Cannot access field!", ex);
				}
			}
			if (Validatable.class.isAssignableFrom(type)) {
				Validatable validatable = (Validatable)value;
				validatable.validate(loc == null? Localization.getInstance(): loc);
			}
		}
	}
	
	private void validateObject(Object value, String name, Validate anno) throws ValidateException {
        pathStack.push(anno.name().isEmpty() ? name: anno.name());
		if (value == null) {
			if (!anno.allowNull())
                throw new ValidateException(ValidateMessageConstant.CANNOT_BE_NULL.getMessage(loc));
		} else {
			Class<?> type = value.getClass();
			validateObject(value, type);
		}
        pathStack.pop();
	}
	
	private void validateCollectionItem(Object item, String name, ValidateCollection anno) throws ValidateException {
		if (item == null) {
			if (!anno.allowNullItem()) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.CANNOT_BE_NULL.getMessage(loc));
            }
		} else {
			Class<?> itemType = anno.itemType();
			if (itemType.isAnnotationPresent(CollectionItem.class)) {
                CollectionItem collectionItem = itemType.getAnnotation(CollectionItem.class);
                Annotation[] annotations = itemType.getAnnotations();
                itemType = collectionItem.type();
                validateInternal(item, itemType, name, annotations);
			} else {
                pathStack.push(name);
				if (!itemType.isInstance(item))
                    throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                            loc, itemType.getName(), item.getClass().getName()));
				validateObject(item, itemType);
                pathStack.pop();
			}
		}
	}
	
	private void validateCollection(Collection<?> value, String name, ValidateCollection anno) throws ValidateException {
        pathStack.push(anno.name().isEmpty() ? name: anno.name());
        if (value == null) {
			if (!anno.allowNull())
                throw new ValidateException(ValidateMessageConstant.CANNOT_BE_NULL.getMessage(loc));
		} else {
            int len = value.size();
            if (anno.minSize() > 0 && anno.maxSize() != Integer.MAX_VALUE) {
                if (len < anno.minSize() || len > anno.maxSize())
                    throw new ValidateException(ValidateMessageConstant.COUNT_SHOULD_BETWEEN.getMessage(loc, anno.minSize(), anno.maxSize()));
            } else if (anno.minSize() > 0) {
                if (len < anno.minSize())
                    throw new ValidateException(ValidateMessageConstant.COUNT_SHOULD_GREAT_THAN.getMessage(loc, anno.minSize()));
            } else if (anno.maxSize() != Integer.MAX_VALUE) {
                if (len > anno.maxSize())
                    throw new ValidateException(ValidateMessageConstant.COUNT_SHOULD_LESS_THAN.getMessage(loc, anno.maxSize()));
            }
			int i = 0;
			for (Object item : value) {
				validateCollectionItem(item, MessageFormat.format("[{0}]",i), anno);
				i++;
			}
		}
        pathStack.pop();
	}
	
	private void validateArray(Object value, String name, ValidateCollection anno) throws ValidateException {
        pathStack.push(anno.name().isEmpty() ? name: anno.name());
		if (value == null) {
			if (!anno.allowNull())
                throw new ValidateException(ValidateMessageConstant.CANNOT_BE_NULL.getMessage(loc));
		} else {
            int len = Array.getLength(value);
            if (anno.minSize() > 0 && anno.maxSize() != Integer.MAX_VALUE) {
                if (len < anno.minSize() || len > anno.maxSize())
                    throw new ValidateException(ValidateMessageConstant.COUNT_SHOULD_BETWEEN.getMessage(loc, anno.minSize(), anno.maxSize()));
            } else if (anno.minSize() > 0) {
                if (len < anno.minSize())
                    throw new ValidateException(ValidateMessageConstant.COUNT_SHOULD_GREAT_THAN.getMessage(loc, anno.minSize()));
            } else if (anno.maxSize() != Integer.MAX_VALUE) {
                if (len > anno.maxSize())
                    throw new ValidateException(ValidateMessageConstant.COUNT_SHOULD_LESS_THAN.getMessage(loc, anno.maxSize()));
            }
			for (int i = 0; i < len; i++) {
				Object item = Array.get(value, i);
				validateCollectionItem(item, MessageFormat.format("[{0}]",i), anno);
			}
		}
        pathStack.pop();
	}
	
	private void validateMap(Map<?,?> value, String name, ValidateMap anno) throws ValidateException {
        pathStack.push(anno.name().isEmpty() ? name: anno.name());
        if (value == null) {
			if (!anno.allowNull())
                throw new ValidateException(ValidateMessageConstant.CANNOT_BE_NULL.getMessage(loc));
		} else {
			Class<?> type = anno.type();
			Set<Field> fieldsToCheck = new HashSet<>();
			fieldsToCheck.addAll(Arrays.asList(type.getFields()));
			fieldsToCheck.addAll(Arrays.asList(type.getDeclaredFields()));
			for (Field field : fieldsToCheck) {
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				String key = field.getName();
				Object obj = value.get(key);
				validateInternal(obj, field.getType(), key, field.getAnnotations());
			}
		}
        pathStack.pop();
	}

    private static Double toDouble(Object obj) {
        if (obj == null)
            return null;
        if (String.class.isInstance(obj)) {
            try {
                return Double.valueOf(obj.toString());
            } catch (Exception e) {
                return null;
            }
        } else
            return numberClass.get(obj.getClass()).convert(obj);
    }
	private static Boolean toBoolean(Object obj) {
		if (obj == null)
			return null;
		else if (obj.getClass().equals(boolean.class))
			return (boolean)obj;
		else if (obj.getClass().equals(Boolean.class))
			return (Boolean)obj;
        else if (String.class.isInstance(obj)) {
            try {
                return Boolean.valueOf(obj.toString());
            } catch (Exception e) {
                return null;
            }
        } else
			return null;
	}
    private static DateTime toDateTime(Object obj) {
        if (obj == null)
            return null;
        else if (obj.getClass().equals(Date.class))
            return new DateTime(((Date)obj).getTime());
        else if (obj.getClass().equals(DateTime.class))
            return (DateTime)obj;
        else {
            try {
                return DateTime.parse(obj.toString());
            } catch (Exception e) {
                return null;
            }
        }
    }
	
	private static boolean isDate(Class<?> type) {
		return type.equals(Date.class) || type.equals(DateTime.class);
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
		else if (Date.class.isInstance(value) || DateTime.class.isInstance(value))
            return true;
        else if (String.class.isInstance(value)) {
            try {
                DateTime.parse(value.toString());
                return true;
            } catch (Exception e) {
                return false;
            }
        } else
            return false;
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
	private <T> T getAnnotation(Annotation[] annotations, Class<T> annoType, Class<?> objType) throws ValidateException {
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
            throw new ValidateException(ValidateMessageConstant.INVALID_VALIDATE_RULE.getMessage(
                    loc, objType.getName()));
	}
	
	private void validateInternal(Object object, Class<?> type, String name, Annotation[] annotations) throws ValidateException {
		if (isString(type)) {
			if (!isStringOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
			ValidateString anno = getAnnotation(annotations, ValidateString.class, type);
			if (anno != null)
				validateString((String)object, name, anno);
		} else if (isNumber(type)) {
			if (!isNumberOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
			ValidateNumber anno = getAnnotation(annotations, ValidateNumber.class, type);
			if (anno != null)
				validateNumber(toDouble(object), name, anno);
		} else if (isBoolean(type)) {
			if (!isBooleanOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
			ValidateBoolean anno = getAnnotation(annotations, ValidateBoolean.class, type);
			if (anno != null)
				validateBoolean(toBoolean(object), name, anno);
		} else if (isDate(type)) {
			if (!isDateOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
			ValidateDate anno = getAnnotation(annotations, ValidateDate.class, type);
			if (anno != null)
				validateDate(toDateTime(object), name, anno);
		} else if (isCollection(type)) {
			if (!isCollectionOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
			ValidateCollection anno = getAnnotation(annotations, ValidateCollection.class, type);
			if (anno != null)
				validateCollection((Collection<?>)object, name, anno);
		} else if (isArray(type)) {
			if (!isArrayOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
			ValidateCollection anno = getAnnotation(annotations, ValidateCollection.class, type);
			if (anno != null)
				validateArray(object, name, anno);
		} else if (isMap(type)) {
			if (!isMapOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
			ValidateMap anno = getAnnotation(annotations, ValidateMap.class, type);
			if (anno != null)
				validateMap((Map<?,?>)object, name, anno);
		} else {
			if (object != null && !type.isInstance(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
			Validate anno = getAnnotation(annotations, Validate.class, type);
			if (anno != null)
				validateObject(object, name, anno);
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
                    public String name() {
                        return "";
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Validate.class;
                    }
                }
        });
    }

    private static final Pattern ARRAY_NAME_PATTERN = Pattern.compile("\\[\\d+\\]");

	public void validate(Object object, Class<?> type, Annotation[] annotations) throws ValidateException {
		try {
			pathStack.clear();
			validateInternal(object, type, null, annotations);
		} catch (Exception ex) {
			StringBuilder sb = new StringBuilder();
            boolean isEmpty = true;
            for (int i = 0; i < pathStack.size(); i++) {
                String name = pathStack.get(i);
                if (name != null && !name.isEmpty()) {
                    if (!isEmpty && !ARRAY_NAME_PATTERN.matcher(name).matches())
                        sb.append(".");
                    if (loc == null)
                        sb.append(name);
                    else
                        sb.append(loc.get(name));
                    isEmpty = false;
                }
            }
            String message = ValidateMessageConstant.VERIFY_FAILED.getMessage(
                    loc, sb.toString(), ex.getMessage());
			throw new ValidateException(message);
		}
    }

}
