package com.github.thorqin.toolkit.web.annotation;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by thor on 5/28/15.
 */
@Retention(value=RUNTIME)
@Target(value={TYPE})
public @interface WebAppService {
    String name();
    Class<?> type();
}
