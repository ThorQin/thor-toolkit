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

package com.github.thorqin.toolkit.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author nuo.qin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
public @interface ValidateString {
	String NUMBER = "^[+\\-]?\\d+(\\.\\d+)?|\\.\\d+$";
	String DIGITAL = "^\\d+$";
	String POSTCODE = "^\\d{6}$";
	String INTEGER = "^[+\\-]?\\d+$";
	String FLOAT = "^[+\\-]?\\d*\\.\\d+$";
	String CURRENCY = "^-?(?:[A-Z]{3}|\\$|€|￥|￡|฿|₩)?\\d{1,3}(,\\d{3})*\\.\\d{2,3}$";
	String USERNAME = "^[a-zA-Z]\\w{5,29}$";
	String EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    String MAIL_BOX = "(" + EMAIL + ")||(^.+\\s+<\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*>$)";
	String CHINESE_CHARACTER = "^[\u4e00-\u9fa5]{0,}$";
	String URL = "^https?://([\\w-]+\\.)+[\\w-]+(:[0-9]{1,5})?(/[\\w-./?%&=]*)?$";
	String HTML_TAG = "<(.*)>(.*)<\\/(.*)>|<(.*)\\/>";
	String DATE = "^[0-9]{4}-(0?[1-9]|1[0-2])-((0?[1-9])|((1|2)[0-9])|30|31)$";
	String TIME = "^([0-1]?[0-9]|2[0-3])(:[0-5]?[0-9]){2}(\\.[0-9]{1,3})?$";
	String DATE_TIME = "^[0-9]{4}-(0?[1-9]|1[0-2])-((0?[1-9])|((1|2)[0-9])|30|31)\\s+(?:[0-1]?[0-9]|2[0-3])(?::[0-5]?[0-9]){2}(\\.[0-9]{1,3})?$";
	String DATE_TIME_WITHOUT_MILLI = "^[0-9]{4}-(0?[1-9]|1[0-2])-((0?[1-9])|((1|2)[0-9])|30|31)\\s+(?:[0-1]?[0-9]|2[0-3])(?::[0-5]?[0-9]){2}$";
	String FULL_DATE = "^[0-9]{4}-(0[1-9]|1[0-2])-((0[1-9])|((1|2)[0-9])|30|31)$";
	String FULL_TIME = "^([0-1][0-9]|2[0-3])(:[0-5][0-9]){2}(\\.[0-9]{3})?$";
	String FULL_TIME_WITHOUT_MILLI = "^([0-1][0-9]|2[0-3])(:[0-5][0-9]){2}$";
	String FULL_DATE_TIME = "^[0-9]{4}-(0[1-9]|1[0-2])-((0[1-9])|((1|2)[0-9])|30|31)\\s+([0-1][0-9]|2[0-3])(:[0-5][0-9]){2}(\\.[0-9]{3})?$";
	String FULL_DATE_TIME_WITHOUT_MILLI = "^[0-9]{4}-(0[1-9]|1[0-2])-((0[1-9])|((1|2)[0-9])|30|31)\\s+([0-1][0-9]|2[0-3])(:[0-5][0-9]){2}$";
	String ISO8601 = "^[0-9]{4}-(0[1-9]|1[0-2])-((0[1-9])|((1|2)[0-9])|30|31)T([0-1][0-9]|2[0-3])(:[0-5][0-9]){2}(\\.[0-9]{3})?(Z|[+-](0[0-9]|1[0-2])(:?([0-5][0-9]))?)$";
	String ISO8601_UTC = "^[0-9]{4}-(0[1-9]|1[0-2])-((0[1-9])|((1|2)[0-9])|30|31)T([0-1][0-9]|2[0-3])(:[0-5][0-9]){2}(\\.[0-9]{3})?Z$";
    String IPV4 = "^[0-9]{1,3}(\\.[0-9]{1,3}){3}$";

	boolean allowNull() default false;
	boolean allowEmpty() default false;
	String[] value() default {};
	int minLength() default 0;
	int maxLength() default Integer.MAX_VALUE;

    /**
     * Will be used in error message if provided.
     * @return Field or object's name or similar text.
     */
    String name() default "";
}
