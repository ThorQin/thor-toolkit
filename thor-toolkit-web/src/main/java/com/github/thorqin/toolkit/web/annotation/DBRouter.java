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
    public String value() default "db";

    /**
     * Framework consider this value is a stored procedure name and
     * try to call it to obtain web entries info.
     * @return Index stored procedure name
     */
    public String index() default "_web_";

    public String refreshEntry() default "refreshRouteTable";
}
