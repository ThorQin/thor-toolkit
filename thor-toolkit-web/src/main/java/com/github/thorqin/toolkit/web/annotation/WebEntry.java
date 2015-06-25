/*
 * The MIT License
 *
 * Copyright 2014 nuo.qin.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.thorqin.toolkit.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebEntry {

    HttpMethod[] method();

    /**
     * Path name, if name is same with the method name then keep this value in empty.
     * @return
     */
    String value() default "";

    /**
     * If set to TRUE then will send cross-site header to client
     * @return
     */
    boolean crossSite() default false;

    /**
     * If multiple path can match the request url,
     * then use order value to define which path have high priority,
     * Rule is: order value smaller have the high priority.
     * @return
     */
    int order() default 10000;

    /**
     * Whether or not use rule search cache.
     * If URL path have PART definition and this PART have plenty of values
     * then you should not use cache otherwise it will expend a lot of memory.
     * for example: if your path is /userInfo/{userName} then you should not use cache,
     * because there maybe have a lot of users in your system. But if your path is
     * /userList/{userType} then you may use the cache in safety.
     * By default, this parameter is set to AUTO,
     * means if there have PART definition then do not use cache.
     *
     * @return
     */
    CacheType cache() default CacheType.AUTO;

}
