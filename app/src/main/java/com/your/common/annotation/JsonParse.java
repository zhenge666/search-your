package com.your.common.annotation;

import java.lang.annotation.*;

/**
 * 解析json转为自定义对象注解
 *
 * @author zhangzhen
 * @Date 2022/6/22 下午3:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface JsonParse {

    String[] allKey() default {};

}