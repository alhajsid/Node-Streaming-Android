package com.example.streaming.library.db.annotation;

import com.example.streaming.library.BaseConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * 字段名称
     */
    String name();

    /**
     * 所有权
     */
    String property() default BaseConstants.NULL;

    /**
     * 是否为ID
     */
    boolean isId() default false;

    /**
     * 是否自生长
     */
    boolean autoGen() default true;
}
