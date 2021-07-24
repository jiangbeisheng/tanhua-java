package com.tanhua.common.utils;

import java.lang.annotation.*;

/**
 * 被标记为cache的controller进行缓存，其他情况不进行缓存
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented //标记注解
public @interface Cache {

    String time() default "60";
}
