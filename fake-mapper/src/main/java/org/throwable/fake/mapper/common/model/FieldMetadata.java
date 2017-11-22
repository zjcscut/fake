package org.throwable.fake.mapper.common.model;

import lombok.Getter;
import lombok.Setter;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author throwable
 * @version v1.0
 * @description 实体field元数据
 * @since 2017/8/15 0:01
 */
@Getter
@Setter
public class FieldMetadata {

    private String name;
    private Field field;
    private Class<?> javaType;
    private Method setter;
    private Method getter;

    //Field to describe field level annotation
    public FieldMetadata(Field field) {
        if (null != field) {
            this.field = field;
            this.name = field.getName();
            this.javaType = field.getType();
        }
    }

    //PropertyDescriptor to describe method level annotation
    public FieldMetadata(PropertyDescriptor descriptor) {
        if (null != descriptor) {
            this.name = descriptor.getName();
            this.setter = descriptor.getWriteMethod();
            this.getter = descriptor.getReadMethod();
            this.javaType = descriptor.getPropertyType();
        }
    }

    public void copy(FieldMetadata other) {
        this.setter = other.setter;
        this.getter = other.getter;
        this.javaType = other.javaType;
        this.name = other.name;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        boolean result = false;
        if (null != field) {
            result = field.isAnnotationPresent(annotationClass);
        }
        if (!result && null != setter) {
            result = setter.isAnnotationPresent(annotationClass);
        }
        if (!result && null != getter) {
            result = getter.isAnnotationPresent(annotationClass);
        }
        return result;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T result = null;
        if (null != field) {
            result = field.getAnnotation(annotationClass);
        }
        if (result == null && null != setter) {
            result = setter.getAnnotation(annotationClass);
        }
        if (result == null && null != getter) {
            result = getter.getAnnotation(annotationClass);
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldMetadata that = (FieldMetadata) o;
        return !(name != null ? !name.equals(that.name) : that.name != null);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
