package com.github.thorqin.toolkit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by thor on 9/1/16.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface App {
    String name();
    Service[] services() default {};
    String appDataEnv() default "app.data.dir";
    String configName() default "config.yml";
}

