/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.thorqin.toolkit.validation.annotation;

import java.lang.annotation.*;

/**
 *
 * @author nuo.qin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CollectionItem {
    Class<?> type();
}
