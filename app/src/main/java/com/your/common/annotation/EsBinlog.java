package com.your.common.annotation;

import com.your.common.enums.BinlogDmlEnum;

import java.lang.annotation.*;

/**
 * ES-监听binlog自定义注解
 *
 * @author zhangzhen
 * @Date 2022/4/18 下午3:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface EsBinlog {

    /**
     * 操作类型
     *
     * @return
     */
    BinlogDmlEnum dml();

    /**
     * 表名数组
     *
     * @return
     */
    Class[] tables();

}