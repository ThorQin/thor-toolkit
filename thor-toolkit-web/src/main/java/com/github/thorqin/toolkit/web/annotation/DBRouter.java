package com.github.thorqin.toolkit.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by nuo.qin on 2/4/2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DBRouter {
    /**
     * Database configuration name in config.json
     * @return Configuration name
     */
    String value() default "db";

    /**
     * Framework consider this value is a stored procedure name and
     * try to call it to obtain web entries info.
     * @return Index stored procedure name
     */
    String index() default "_web_";

    /**
     * Provide ability that can refresh database router table at runtime.
     * Set this value to empty if do not need this feature.
     * @return Name of the entry
     */
    String refreshEntry() default "";

    /**
     * Use specified properties file to translate DB raised http message.
     * @return Message that translated in specified locale
     */
    String localeMessage() default "message";

    /**
     * If set configName then use configManager load index mapping from config instead load from database.
     * @return Config name(path)
     */
    String configName() default "";
}
