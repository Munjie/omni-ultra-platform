package com.munjie.omni.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    // 每秒允许的请求数
    int limit() default 10;
    // 时间窗口（秒）
    int duration() default 1;
}