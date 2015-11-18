/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public @interface ValidateDate {
	boolean allowNull() default false;

    /**
     * Use ISO8601 Format
     * @return Minimum date time value
     */
	String min() default "1970-1-1";

    /**
     * Use ISO8601 Format (e.g.: '1970-01-01T23:00:00Z', '1970-01-01T23:00:00+08')
     * @return Maximum date time value
     */
	String max() default "";
    /**
     * Will be used in error message if provided.
     * @return Field or object's name or similar text.
     */
    String name() default "";
}
