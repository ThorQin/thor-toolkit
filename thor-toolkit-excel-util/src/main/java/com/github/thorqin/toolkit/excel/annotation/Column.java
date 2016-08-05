package com.github.thorqin.toolkit.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by thor on 7/29/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface  Column {

    /**
     * Column name
     * @return name of the column
     */
    String value();

    boolean required() default false;
    /**
     * Which other columns need this column
     * @return Columns which need this column.
     */
    String[] requiredBy() default {};
    /**
     * Only used by ExcelParser
     * @return Column format regular expression
     */
    String[] format() default {};
    /**
     * Only used by ExcelParser, if cell format invalid then use this message to indicate what error raised.
     * @return Column format description
     */
    String formatDescription() default "";



    /**
     * Only used by ExcelWriter
     * @return Column display order
     */
    int order() default 0;
    /**
     * Only used by ExcelWriter
     * @return Column width, it's unit is characters, e.g.: 20 means 20 characters
     */
    int width() default -1;

}
