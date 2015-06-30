package com.github.thorqin.toolkit.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by thor on 5/28/15.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Service {
    /**
     * Service name must provide a valid and unique name
     * @return
     */
    String value();

    /**
     * Service class type
     * @return
     */
    Class<?> type() default Object.class;

    /**
     * Indicate whether or not only apply to specified application,
     * NOTE: this value only effect service registration by directly annotated service definition.
     * @return Application name that provided by WebApplication instance.
     */
    String application() default "";
}
