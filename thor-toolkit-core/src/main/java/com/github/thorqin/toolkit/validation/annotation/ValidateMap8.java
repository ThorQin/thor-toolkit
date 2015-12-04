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
 * Validate object as an Map
 * @author nuo.qin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
public @interface ValidateMap8 {
	boolean allowNull() default false;
    /**
     * Minimum size of the map
     * @return Value of the minimum size.
     */
    int minSize() default 0;

    /**
     * Maximum size of the map
     * @return Value of the maximum size.
     */
    int maxSize() default Integer.MAX_VALUE;

    /**
     * If provided, check each key by this format regexp expression.
     * ignore checking if this value is empty.
     * @return regexp expression, each key will be checked by matches() method using this expression.
     */
    String keyRule() default "";

    /**
     * Check the map, make sure the map contains all of the keys defined in this array.
     * @return Key names array.
     */
    String[] needKeys() default {};

    /**
     * If TRUE, then this map object will be checked as an entity
     * and type field will indicate which the entity class will be used as the checking rule.<br/>
     * If FALSE, then will check each element whether
     * matches the type definition and it's related validation rule.<br/>
     * Default is TRUE
     * @return Value indicate whether checked as an entity.
     */
    boolean asEntity() default true;

    /**
     * Define entity class or element class
     * @see #asEntity()
     * @return class definition
     */
	Class<?> type();
}
