package com.github.thorqin.toolkit.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by thor on 7/29/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface  Column {
    String value();
    int order() default 0;
    boolean required() default false;
    String[] requiredBy() default {};
    String[] format() default {};
    String formatDescription() default "";
}
