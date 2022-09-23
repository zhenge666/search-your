package com.your.common.annotation;

import org.elasticsearch.search.sort.SortOrder;

import java.lang.annotation.*;

/**
 * ES-响应结果自定义注解
 *
 * @author zhangzhen
 * @Date 2022/4/2 下午3:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface EsResult {

    /**
     * 对应的索引字段，如果需要排序不能为空
     *
     * @return
     */
    String name() default "";

    /**
     * 包含字段,要么整个对象都配，要么整个不要配，配哪些字段响应哪些字段,
     * 仅在对象很大且使用字段很少的情况下使用
     *
     * @return
     */
    String[] includes() default {};

    /**
     * 排序优先级, 0表示不排序
     *
     * @return
     */
    int sort() default 0;

    /**
     * 正序倒序
     *
     * @return
     */
    SortOrder order() default SortOrder.DESC;

}