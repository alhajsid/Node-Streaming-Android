package com.example.streaming.library.db.annotation;

import com.example.streaming.library.BaseConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * 表名
     */
    String name();

    /**
     * 是否创建
     */
    String onCreated() default BaseConstants.NULL;
}

