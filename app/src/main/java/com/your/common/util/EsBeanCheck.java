package com.your.common.util;

import com.alibaba.fastjson.annotation.JSONField;
import com.your.business.search.es.dto.EsBaseEntity;
import com.your.common.constant.EsConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 校验ES对象是否符合要求
 *
 * @author zhangzhen
 * @Date 2022/4/8 上午11:38
 */
@Slf4j
public class EsBeanCheck {

    /**
     * 校验ES对象字段注解是否正确
     *
     * @param esClass
     */
    public static void checkFiled(Class esClass) {
        Field[] fields = esClass.getDeclaredFields();
        if (fields == null || fields.length == 0) {
            throw new RuntimeException(String.format("%s, 没有ES字段", esClass.getSimpleName()));
        }
        // 如果是entity包下的，需要继承基类
        if (esClass.getName().contains(".entity.") && !esClass.getSuperclass().equals(EsBaseEntity.class)) {
            throw new RuntimeException(String.format("%s, 没有继承EsBaseEntity", esClass.getSimpleName()));
        }
        for (Field field : fields) {
            // 排除系统字段
            if (field.getName().contains("serialVersionUID")) {
                continue;
            }
            org.springframework.data.elasticsearch.annotations.Field annotion = field.getAnnotation(org.springframework.data.elasticsearch.annotations.Field.class);
            if (annotion == null) {
                throw new RuntimeException(String.format("%s.%s, 没有配置ES字段注解", esClass.getSimpleName(), field.getName()));
            }
            // 如果是对象类型，递归检查
            if (Objects.equals(annotion.type(), FieldType.Object)) {
                checkFiled(field.getType());
                continue;
            }
            // 如果是嵌套集合类型，找到对应的class类型，递归检查
            if (Objects.equals(annotion.type(), FieldType.Nested)) {
                checkFiled(EsBeanUtil.getTClass(field));
                continue;
            }
            Class typeClass = field.getType();
            boolean match = false;
            // 如果是字符串，必须是Text、Keyword、Ip
            if (typeClass.getName().equals(String.class.getName())) {
                match = Objects.equals(annotion.type(), FieldType.Text)
                        || Objects.equals(annotion.type(), FieldType.Keyword)
                        || Objects.equals(annotion.type(), FieldType.Ip);
            } else if (typeClass.getName().equals(LocalDateTime.class.getName())) {
                // 如果是时间类型，必须是Date
                match = Objects.equals(annotion.type(), FieldType.Date);
                // 指定格式化形式
                if (annotion.format() == null || !Objects.equals(annotion.format(), DateFormat.date_hour_minute_second_millis)) {
                    throw new RuntimeException(String.format("%s.%s, format需指定为:date_hour_minute_second_millis", esClass.getSimpleName(), field.getName()));
                }
                JSONField jsonField = field.getAnnotation(JSONField.class);
                if (jsonField == null || jsonField.format() == null || !Objects.equals(jsonField.format(), EsConstant.TIME_FORMAT)) {
                    throw new RuntimeException(String.format("%s.%s, 日期格式需配置注解：@JSONField(format = %s)", esClass.getSimpleName(), field.getName(), EsConstant.TIME_FORMAT));
                }
            } else if (typeClass.getName().equals(Integer.class.getName())) {
                // 如果是必须是Integer类型，必须是Integer
                match = Objects.equals(annotion.type(), FieldType.Integer);
            } else if (typeClass.getName().equals(Long.class.getName())) {
                // 如果是Long类型，必须是Long
                match = Objects.equals(annotion.type(), FieldType.Long);
            } else {
                // 如果有其他类型，继续扩展，未知类型，抛出错误
                throw new RuntimeException(String.format("%s.%s, 属性类型不在校验范围内，请扩展校验方法(EsBeanCheck)", esClass.getSimpleName(), field.getName()));
            }
            if (!match) {
                throw new RuntimeException(String.format("%s.%s, 属性类型和ES注解类型不匹配", esClass.getSimpleName(), field.getName()));
            }
        }
    }

    /**
     * 从spring的动态代理类获取本类
     *
     * @param springBean
     * @return
     */
    public static Class getBeanBySpring(Class springBean) {
        try {
            String className = springBean.getName();
            int index = className.indexOf("$");
            if (index > 0) {
                className = className.substring(0, index);
            }
            return Class.forName(className);
        } catch (Exception ex) {
            throw new RuntimeException(String.format("%s, 获取原生class失败", springBean.getName()));
        }
    }

}
