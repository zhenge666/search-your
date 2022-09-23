package com.your.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * ES工具类
 *
 * @author zhangzhen
 * @Date 2022/4/20 下午3:53
 */
@Slf4j
public class EsBeanUtil {

    /**
     * 反射找到指定的字段获取值
     *
     * @param arg
     * @param fieldName
     * @return
     */
    public static Object getFieldValue(Object arg, String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            return null;
        }
        Object fieldValue = null;
        try {
            Field field = ReflectionUtils.findField(arg.getClass(), fieldName);
            if (field != null) {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), arg.getClass());
                // 获取get方法
                Method getMethod = pd.getReadMethod();
                fieldValue = ReflectionUtils.invokeMethod(getMethod, arg);
            }
        } catch (Exception ex) {
            log.error("反射取值失败:", ex);
        }
        return fieldValue;
    }

    /**
     * 反射找到指定的字段赋值
     *
     * @param arg
     * @param fieldName
     * @return
     */
    public static Object setFieldValue(Object arg, String fieldName, Object... values) {
        if (StringUtils.isBlank(fieldName)) {
            return null;
        }
        Object fieldValue = null;
        try {
            Field field = ReflectionUtils.findField(arg.getClass(), fieldName);
            if (field != null) {
                PropertyDescriptor pd = new PropertyDescriptor(field.getName(), arg.getClass());
                // 获取get方法
                Method getMethod = pd.getWriteMethod();
                fieldValue = ReflectionUtils.invokeMethod(getMethod, arg, values);
            }
        } catch (Exception ex) {
            log.error("反射取值失败:", ex);
        }
        return fieldValue;
    }

    /**
     * 驼峰转下划线
     *
     * @param name
     * @return
     */
    public static String camelToUnderline(String name) {
        return com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(name);
    }

    /**
     * 获取类的范型
     *
     * @param target
     * @return
     */
    public static Class<?> getTClass(Class<?> target) {
        return (Class<?>) ((ParameterizedType) target.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * 获取字段的范型
     *
     * @param field
     * @return
     */
    public static Class<?> getTClass(Field field) {
        return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    /**
     * 过滤list的空对象输出set集合
     *
     * @param value
     * @return
     */
    public static Set<?> filterNull(Collection value) {
        if (CollectionUtils.isEmpty(value)) {
            return new HashSet<>(0);
        }
        Set<Object> set = new HashSet<>(value.size());
        for (Object val : value) {
            if (val != null && StringUtils.isNotBlank(val.toString())) {
                set.add(val);
            }
        }
        return set;
    }

    public static <T> List<T> filterNullToList(List<T> value) {
        if (CollectionUtils.isEmpty(value)) {
            return new ArrayList<>(0);
        }
        List<T> set = new ArrayList<>(value.size());
        for (T t : value) {
            if (t != null) {
                set.add(t);
            }
        }
        return set;
    }

    /**
     * 比较binlog的执行时间，如果当前时间小于
     *
     * @param nowTs 当前ES类中的时间
     * @param msgTs 最新binlog消息的时间
     * @return true:执行，false：不执行
     */
    public static boolean isUpdate(Long nowTs, Long msgTs) {
        if (nowTs > msgTs) {
            return false;
        }
        return true;
    }

}
