package com.your.common.annotation;



import com.your.common.enums.StatsTypeEnum;

import java.lang.annotation.*;

/**
 * ES-聚合查询自定义注解
 *
 * @author zhangzhen
 * @Date 2022/4/14 下午3:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface EsStats {

    /**
     * 查询类型
     *
     * @return
     */
    StatsTypeEnum type();

    /**
     * 聚合的字段名称
     *
     * @return
     */
    String fieldName();

    /**
     * 筛选的起始字段
     *
     * @return
     */
    String startField() default "";

    /**
     * 筛选的结束字段
     *
     * @return
     */
    String endField() default "";

}