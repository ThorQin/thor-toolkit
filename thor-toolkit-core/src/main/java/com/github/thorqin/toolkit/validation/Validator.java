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
import java.lang.reflect.InvocationTargetException;
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
					validateInternal(field.get(value), field.getType(), fieldName, field.getAnnotations(), 0);
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

	private void validateCollection(Collection<?> value, String name, ValidateCollection anno, Annotation[] annotations, int level) throws ValidateException {
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
                validateInternal(item, anno.type(), MessageFormat.format("[{0}]",i), annotations, level + 1);
				i++;
			}
		}
        pathStack.pop();
	}
	
	private void validateArray(Object value, String name, ValidateCollection anno, Annotation[] annotations, int level) throws ValidateException {
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
                validateInternal(item, anno.type(), MessageFormat.format("[{0}]",i), annotations, level + 1);
			}
		}
        pathStack.pop();
	}
	
	private void validateMap(Map<?,?> value, String name, ValidateMap anno, Annotation[] annotations, int level) throws ValidateException {
        pathStack.push(anno.name().isEmpty() ? name: anno.name());
        if (value == null) {
			if (!anno.allowNull())
                throw new ValidateException(ValidateMessageConstant.CANNOT_BE_NULL.getMessage(loc));
		} else {
            // Validate size
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
            // Validate needed keys
            for (String key: anno.needKeys()) {
                if (!value.containsKey(key)) {
                    throw new ValidateException(ValidateMessageConstant.NEED_KEY.getMessage(loc, key));
                }
            }
            // Validate key rule.
            if (!anno.keyRule().isEmpty()) {
                Pattern keyRule;
                try {
                    keyRule = Pattern.compile(anno.keyRule());
                } catch (Exception e) {
                    throw new ValidateException(ValidateMessageConstant.INVALID_KEY_RULE.getMessage(loc));
                }
                for (Object key: value.keySet()) {
                    if (!keyRule.matcher(key.toString()).matches()) {
                        throw new ValidateException(ValidateMessageConstant.INVALID_KEY.getMessage(loc, key));
                    }
                }
            }

            if (anno.asEntity()) {
                Class<?> type = anno.type();
                Set<Field> fieldsToCheck = new HashSet<>();
                fieldsToCheck.addAll(Arrays.asList(type.getFields()));
                fieldsToCheck.addAll(Arrays.asList(type.getDeclaredFields()));
                for (Field field : fieldsToCheck) {
                    if (Modifier.isStatic(field.getModifiers()))
                        continue;
                    String key = field.getName();
                    Object obj = value.get(key);
                    validateInternal(obj, field.getType(), key, field.getAnnotations(), 0);
                }
            } else {
                for (Object key: value.keySet()) {
                    Object item = value.get(key);
                    validateInternal(item, anno.type(), MessageFormat.format("[{0}]", key),
                            annotations, level + 1);
                }
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
    private static <T> T getAnnoValue(Annotation annotation, String methodName) {
		try {

            return (T)annotation.getClass().getMethod(methodName).invoke(annotation);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ValidateCollection toCollectionAnnotation(final Annotation annotation, int level) {
        if (ValidateCollection.class.isInstance(annotation) && level == 0)
            return (ValidateCollection)annotation;
        else if (annotation.annotationType().getSimpleName().equals(ValidateCollection.class.getSimpleName() + level)) {
            return new ValidateCollection() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return ValidateCollection.class;
                }

                @Override
                public boolean allowNull() {
                    return getAnnoValue(annotation, "allowNull");
                }

                @Override
                public int minSize() {
                    return getAnnoValue(annotation, "minSize");
                }

                @Override
                public int maxSize() {
                    return getAnnoValue(annotation, "maxSize");
                }

                @Override
                public Class<?> type() {
                    return getAnnoValue(annotation, "type");
                }

                @Override
                public String name() {
                    return "";
                }
            };
        } else {
            return null;
        }
    }

    private ValidateMap toMapAnnotation(final Annotation annotation, int level) {
        if (ValidateMap.class.isInstance(annotation) && level == 0)
            return (ValidateMap)annotation;
        else if (annotation.annotationType().getSimpleName().equals(ValidateCollection.class.getSimpleName() + level)) {
            return new ValidateMap() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return ValidateMap.class;
                }

                @Override
                public boolean allowNull() {
                    return getAnnoValue(annotation, "allowNull");
                }

                @Override
                public int minSize() {
                    return getAnnoValue(annotation, "minSize");
                }

                @Override
                public int maxSize() {
                    return getAnnoValue(annotation, "maxSize");
                }

                @Override
                public String keyRule() {
                    return getAnnoValue(annotation, "keyRule");
                }

                @Override
                public String[] needKeys() {
                    return getAnnoValue(annotation, "needKeys");
                }

                @Override
                public boolean asEntity() {
                    return getAnnoValue(annotation, "asEntity");
                }

                @Override
                public Class<?> type() {
                    return getAnnoValue(annotation, "type");
                }

                @Override
                public String name() {
                    return "";
                }
            };
        } else {
            return null;
        }
    }

    private static class FindResult<T> {
        public T target;
        public Annotation[] annotations;
        public FindResult(T target, Annotation[] annotations, Annotation exclude) {
            this.target = target;
            this.annotations = new Annotation[annotations.length - 1];
            int i = 0;
            for (Annotation item: annotations) {
                if (!item.equals(exclude))
                    this.annotations[i++] = item;
            }
        }
    }

	@SuppressWarnings("unchecked")
	private <T> FindResult<T> getAnnotation(Annotation[] annotations, Class<T> annoType, Class<?> objType, int level) throws ValidateException {
		boolean hasOtherType = false;
		for (Annotation anno: annotations) {
            if (!anno.annotationType().getPackage().equals(Validate.class.getPackage())) {
                continue;
            }
            hasOtherType = true;
            if (annoType.equals(ValidateCollection.class)) {
                Annotation target = toCollectionAnnotation(anno, level);
                if (target != null) {
                    return new FindResult<>((T)target, annotations, anno);
                }
            } else if (annoType.equals(ValidateMap.class)) {
                Annotation target = toMapAnnotation(anno, level);
                if (target != null) {
                    return new FindResult<>((T)target, annotations, anno);
                }
            } else if (annoType.isInstance(anno)) {
                return new FindResult<>((T)anno, annotations, anno);
            }
		}
        if (!hasOtherType)
			return null;
		else
            throw new ValidateException(
                    ValidateMessageConstant.INVALID_VALIDATION_RULE.getMessage(loc, objType.getName()));
	}

    private static Annotation[] exclude(Annotation[] annotations, Annotation excludeAnno) {
        Annotation[] result = new Annotation[annotations.length - 1];
        int i = 0;
        for (Annotation anno: annotations) {
            if (!anno.equals(excludeAnno))
                result[i++] = anno;
        }
        return result;
    }
	
	private void validateInternal(Object object, Class<?> type, String name, Annotation[] annotations, int level) throws ValidateException {
		if (isString(type)) {
			if (!isStringOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
            FindResult<ValidateString> result = getAnnotation(annotations, ValidateString.class, type, level);
			if (result != null)
				validateString((String)object, name, result.target);
		} else if (isNumber(type)) {
			if (!isNumberOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
            FindResult<ValidateNumber> result = getAnnotation(annotations, ValidateNumber.class, type, level);
			if (result != null)
				validateNumber(toDouble(object), name, result.target);
		} else if (isBoolean(type)) {
			if (!isBooleanOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
            FindResult<ValidateBoolean> result = getAnnotation(annotations, ValidateBoolean.class, type, level);
			if (result != null)
				validateBoolean(toBoolean(object), name, result.target);
		} else if (isDate(type)) {
			if (!isDateOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
            FindResult<ValidateDate> result = getAnnotation(annotations, ValidateDate.class, type, level);
			if (result != null)
				validateDate(toDateTime(object), name, result.target);
		} else if (isCollection(type)) {
			if (!isCollectionOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
            FindResult<ValidateCollection> result = getAnnotation(annotations, ValidateCollection.class, type, level);
			if (result != null)
				validateCollection((Collection<?>)object, name, result.target, result.annotations, level);
		} else if (isArray(type)) {
			if (!isArrayOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
            FindResult<ValidateCollection> result = getAnnotation(annotations, ValidateCollection.class, type, level);
			if (result != null)
				validateArray(object, name, result.target, result.annotations, level);
		} else if (isMap(type)) {
			if (!isMapOrNull(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
            FindResult<ValidateMap> result = getAnnotation(annotations, ValidateMap.class, type, level);
			if (result != null)
				validateMap((Map<?,?>)object, name, result.target, result.annotations, level);
		} else {
			if (object != null && !type.isInstance(object)) {
                pathStack.push(name);
                throw new ValidateException(ValidateMessageConstant.INVALID_TYPE.getMessage(
                        loc, type.getName(), object.getClass().getName()));
            }
            FindResult<Validate> result = getAnnotation(annotations, Validate.class, type, level);
			if (result != null)
				validateObject(object, name, result.target);
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

    private static final Pattern ARRAY_NAME_PATTERN = Pattern.compile("\\[.+\\]");

	public void validate(Object object, Class<?> type, Annotation[] annotations) throws ValidateException {
		try {
			pathStack.clear();
			validateInternal(object, type, null, annotations, 0);
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
