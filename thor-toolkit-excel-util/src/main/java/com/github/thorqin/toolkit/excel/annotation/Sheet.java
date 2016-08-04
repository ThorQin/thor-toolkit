package com.github.thorqin.toolkit.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by thor on 8/3/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Sheet {
    String value();
    int headerLine() default 0;
    int order() default 0;
    boolean required() default false;
    String[] requiredBy() default {};
}
