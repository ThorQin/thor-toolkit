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
public @interface ValidateNumber {
	boolean allowNull() default false;
	double min() default Double.MIN_VALUE;
	double max() default Double.MAX_VALUE;
	double[] value() default {};
    /**
     * Will be used in error message if provided.
     * @return Field or object's name or similar text.
     */
    String name() default "";
}
