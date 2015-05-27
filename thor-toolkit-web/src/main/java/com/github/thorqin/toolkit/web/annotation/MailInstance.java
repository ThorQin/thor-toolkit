/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.web.annotation;

import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 *
 * @author nuo.qin
 */
@Retention(value=RUNTIME)
@Target(value={FIELD})
public @interface MailInstance {
    /**
     * Mail service configuration name in config.json
     * @return Configuration name
     */
	String value() default "mail";
}
