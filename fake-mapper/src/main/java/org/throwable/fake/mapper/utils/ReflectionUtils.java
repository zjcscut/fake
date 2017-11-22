package org.throwable.fake.mapper.utils;

import com.google.common.collect.Lists;
import org.throwable.fake.mapper.exception.MetadataParseException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 0:56
 */
public abstract class ReflectionUtils {

	public static boolean isPrimitiveWrappedClass(Class<?> clazz) {
		try {
			return ((Class<?>) clazz.getDeclaredField("TYPE").get(null)).isPrimitive();
		} catch (Exception e) {
			return false;
		}
	}

	public static List<Field> getFieldsAnnotatedWith(Class<?> clazz, Class<? extends Annotation> annotation) {
		AssertUtils.notNull(clazz, "class to getFieldsAnnotatedWith  must not be null!");
		AssertUtils.notNull(annotation, "annotation to getFieldsAnnotatedWith  must not be null!");
		AssertUtils.isTrue(clazz.isAnnotationPresent(annotation), "class to getFieldsAnnotatedWith must be annotated with %s", annotation);
		LinkedList<Field> fields = Lists.newLinkedList();
		for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				if (!Object.class.equals(clazz)
						|| (!Map.class.isAssignableFrom(clazz)
						&& !Collection.class.isAssignableFrom(clazz))) {
					Field[] fieldArrays = clazz.getDeclaredFields();
					for (Field field : fieldArrays) {
						fields.addFirst(field);
					}
				}
			} catch (Exception e) {
				//ignore
			}
		}
		return fields;
	}

	public static List<PropertyDescriptor> getAvailablePropertyDescriptors(Class<?> clazz) {
		BeanInfo beanInfo;
		try {
			beanInfo = Introspector.getBeanInfo(clazz);
		} catch (IntrospectionException e) {
			throw new MetadataParseException(e);
		}
		List<PropertyDescriptor> descriptors = Lists.newArrayList(beanInfo.getPropertyDescriptors());
		descriptors.removeIf(descriptor -> "class".equals(descriptor.getName())); //#exclude "class" descriptor
		return descriptors;
	}

	public static boolean setFieldValueByReflection(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(target, value);
		field.setAccessible(false);
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T> void doInReflection(Object target,
										  String fieldName,
										  FieldFunction<T> fieldFunction) throws NoSuchFieldException, IllegalAccessException {
		Field field = target.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		fieldFunction.apply(field, (T) target);
		field.setAccessible(false);
	}

	@FunctionalInterface
	public interface FieldFunction<T> {

		void apply(Field field, T t) throws IllegalAccessException;
	}
}
