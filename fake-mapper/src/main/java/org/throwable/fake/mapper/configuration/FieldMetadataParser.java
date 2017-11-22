package org.throwable.fake.mapper.configuration;

import com.google.common.collect.Lists;
import org.throwable.fake.mapper.common.annotation.Table;
import org.throwable.fake.mapper.common.model.FieldMetadata;
import org.throwable.fake.mapper.utils.ReflectionUtils;

import java.beans.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 0:48
 */
public abstract class FieldMetadataParser {

    public static List<FieldMetadata> getMergeFieldMetadata(Class<?> entityClass) {
        List<FieldMetadata> fromFields = getFieldMetadataFromFields(entityClass);
        List<FieldMetadata> fromPropertyDescriptors = getFieldMetadataFromPropertyDescriptors(entityClass);
        List<FieldMetadata> mergeFieldMetadata = Lists.newLinkedList();
        for (FieldMetadata fieldMetadata : fromFields) {
            for (FieldMetadata propertyDescriptorMetadata : fromPropertyDescriptors) {
                if (fieldMetadata.getName().equals(propertyDescriptorMetadata.getName())) {
                    fieldMetadata.copy(propertyDescriptorMetadata);
                    break;
                }
            }
            mergeFieldMetadata.add(fieldMetadata);
        }
        return mergeFieldMetadata;
    }

    private static List<FieldMetadata> getFieldMetadataFromFields(Class<?> entityClass) {
        List<Field> fields = ReflectionUtils.getFieldsAnnotatedWith(entityClass, Table.class);
        return fields.stream()
                .filter(field -> !Modifier.isStatic(field.getModifiers())   //exclude static field
                        && !Modifier.isTransient(field.getModifiers())      //exclude transient field
                        && !field.isAnnotationPresent(Transient.class))     //exclude @Transient field
                .map(FieldMetadata::new).collect(Collectors.toList());
    }

    private static List<FieldMetadata> getFieldMetadataFromPropertyDescriptors(Class<?> entityClass) {
        return ReflectionUtils.getAvailablePropertyDescriptors(entityClass).stream()
                .map(FieldMetadata::new).collect(Collectors.toList());
    }
}
