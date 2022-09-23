package com.your.common.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.Loader;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.EnumMemberValue;
import lombok.SneakyThrows;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 动态修改类
 *
 * @author zhangzhen
 * @Date 2022/4/8 上午11:38
 */
public class JavaSsistUtil {

    /**
     * 动态添加class字段注解,暂不启用
     *
     * @param esClass
     * @param annotationName
     * @return
     */
    @SneakyThrows
    @Deprecated
    public static Class<?> addFiledAnnotation(Class esClass, String annotationName) {
        ClassPool classPool = ClassPool.getDefault();
        CtClass clz = classPool.get(esClass.getName());
        CtField[] fields = clz.getDeclaredFields();
        for (CtField field : fields) {
            if (field.getName().contains("serialVersionUID")) {
                continue;
            }
            List<AttributeInfo> attributes = field.getFieldInfo().getAttributes();
            boolean isHave = false;
            if (!attributes.isEmpty()) {
                for (AttributeInfo attribute : attributes) {
                    if (attribute instanceof AnnotationsAttribute) {
                        AnnotationsAttribute bute = (AnnotationsAttribute) attribute;
                        if (bute.getAnnotation(annotationName) != null) {
                            isHave = true;
                            break;
                        }
                    }
                }
            }
            if (isHave) {
                continue;
            }
            AnnotationsAttribute annotationsAttribute = null;
            if (!attributes.isEmpty()) {
                for (AttributeInfo attribute : attributes) {
                    if (attribute instanceof AnnotationsAttribute) {
                        annotationsAttribute = (AnnotationsAttribute) attribute;
                        break;
                    }
                }
            }
            if (annotationsAttribute == null) {
                annotationsAttribute = new AnnotationsAttribute(field.getFieldInfo().getConstPool(), AnnotationsAttribute.visibleTag);
            }
            Annotation annotation = new Annotation(annotationName, field.getFieldInfo().getConstPool());
            EnumMemberValue typeValue = new EnumMemberValue(field.getFieldInfo().getConstPool());
            CtClass ct = field.getType();
            if (ct.getName().equals(Long.class.getName())) {
                typeValue.setValue(FieldType.Long.name());
            } else if (ct.getName().equals(Integer.class.getName())) {
                typeValue.setValue(FieldType.Integer.name());
            }
            typeValue.setType("org.springframework.data.elasticsearch.annotations.FieldType");
            annotation.addMemberValue("type", typeValue);
            annotationsAttribute.addAnnotation(annotation);
            field.getFieldInfo().addAttribute(annotationsAttribute);
        }
        Loader classLoader = new Loader(classPool);
        Class clazz = classLoader.loadClass(esClass.getName());
        for (Field field : clazz.getDeclaredFields()) {
            for (java.lang.annotation.Annotation annotation : field.getAnnotations()) {
                System.out.println(annotation);
            }
        }
        return esClass;
    }

    @SneakyThrows
    public static String toJsonString(Class<?> type) {
        SerializerFeature[] serializer = {SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullNumberAsZero,
                SerializerFeature.WriteNullListAsEmpty, SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse};
        Object o = type.newInstance();
        return JSONObject.toJSONString(o, serializer);
    }

    public static void main(String[] args) {
        Class<?> aClass = addFiledAnnotation(User.class, "org.springframework.data.elasticsearch.annotations.Field");
        System.out.println(toJsonString(aClass));
    }

}