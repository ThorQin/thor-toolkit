
package com.github.thorqin.toolkit.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author nuo.qin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Entity {

    SourceType source() default SourceType.EITHER;

	Encoding encoding() default Encoding.EITHER;

}
