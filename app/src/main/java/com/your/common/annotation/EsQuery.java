package com.your.common.annotation;



import com.your.common.enums.QueryTypeEnum;

import java.lang.annotation.*;

/**
 * ES-查询条件自定义注解
 *
 * @author zhangzhen
 * @Date 2022/4/2 下午3:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface EsQuery {

    /**
     * 条件名称
     *
     * @return
     */
    String name();

    /**
     * 条件类型
     *
     * @return
     */
    QueryTypeEnum queryType() default QueryTypeEnum.TERMS;

    /**
     * 开启嵌套查询
     *
     * @return
     */
    boolean nestedOpen() default false;

    /**
     * 嵌套查询的path
     *
     * @return
     */
    String nestedPath() default "";

}