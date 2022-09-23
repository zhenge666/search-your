package com.your.common.annotation;

import com.your.business.search.es.init.InitBaseCreator;

import java.lang.annotation.*;

/**
 * ES-启动自定义注解
 *
 * @author zhangzhen
 * @Date 2022/4/2 下午3:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface EsStart {

    /**
     * 对象名称
     *
     * @return
     */
    String name();

    /**
     * 初始化数据的类
     *
     * @return
     */
    Class<? extends InitBaseCreator> initClass();

}