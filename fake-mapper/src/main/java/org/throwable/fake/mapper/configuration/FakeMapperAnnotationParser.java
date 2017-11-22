package org.throwable.fake.mapper.configuration;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.throwable.fake.mapper.common.constant.Constants;
import org.throwable.fake.mapper.exception.MetadataParseException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/16 23:57
 */
public class FakeMapperAnnotationParser {

    @Getter
    private Set<Class<? extends Annotation>> includeAnnotations = Sets.newHashSetWithExpectedSize(4);
    @Getter
    private Set<String> includeMethodNames = Sets.newHashSet();

    public FakeMapperAnnotationParser() {
        registerDefaultIncludeAnnotations();
    }

    private void registerDefaultIncludeAnnotations() {
        this.includeAnnotations.add(SelectProvider.class);
        this.includeAnnotations.add(InsertProvider.class);
        this.includeAnnotations.add(UpdateProvider.class);
        this.includeAnnotations.add(DeleteProvider.class);
    }

    public void setIncludeAnnotations(Set<Class<? extends Annotation>> includeAnnotations) {
        this.includeAnnotations.clear();
        this.includeAnnotations.addAll(includeAnnotations);
    }

    public Set<MapperMethodMetadata> doParse(Class<?> mapperClass) {
        Set<MapperMethodMetadata> mapperMethodMetadata = new HashSet<>();
        Class<?>[] genericClass = findGenericClassTypeFromMapperClass(mapperClass);
        if (null != genericClass) {
            doParsePairRecursively(mapperClass, mapperMethodMetadata, mapperClass, genericClass);
        }
        return mapperMethodMetadata;
    }

    private void doParsePairRecursively(Class<?> subMapperClass,
                                        Set<MapperMethodMetadata> methodMetadataSet,
                                        Class<?> mapperClass,
                                        Class<?> genericClass[]) {
        Method[] methods = subMapperClass.getDeclaredMethods();
        if (null != methods && methods.length > 0) {
            for (Method method : methods) {
                for (Class<? extends Annotation> annotation : includeAnnotations) {
                    if (method.isAnnotationPresent(annotation) && checkProviderHasFakeField(method, annotation)) {
                        methodMetadataSet.add(new MapperMethodMetadata(method, mapperClass, genericClass[0], genericClass[1]));
                        includeMethodNames.add(method.getName());
                        break;
                    }
                }
            }
        }
        Class<?>[] interfaces = subMapperClass.getInterfaces();
        if (null != interfaces && interfaces.length > 0) {
            for (Class<?> interfaceToParse : interfaces) {
                doParsePairRecursively(interfaceToParse, methodMetadataSet, mapperClass, genericClass);
            }
        }
    }

    private Class<?>[] findGenericClassTypeFromMapperClass(final Class<?> mapperClass) {
        Type[] genericInterfaces = mapperClass.getGenericInterfaces();
        if (null != genericInterfaces && genericInterfaces.length > 0) {
            return Stream.of(genericInterfaces)
                    .filter(type -> type instanceof ParameterizedType)
                    .map(type -> (ParameterizedType) type)
                    .filter(type -> type.getRawType() == mapperClass || ((Class<?>) type.getRawType()).isAssignableFrom(mapperClass))
                    .findFirst()
                    .map(type -> new Class<?>[]{(Class<?>) type.getActualTypeArguments()[0], (Class<?>) type.getActualTypeArguments()[1]})
                    .orElseThrow(() -> new MetadataParseException(String.format("Fetch generic class type on mapper class [%s] failed!", mapperClass.getCanonicalName())));
        }
        throw new MetadataParseException(String.format("Fetch generic class type on mapper class [%s] failed!", mapperClass.getCanonicalName()));
    }

    private boolean checkProviderHasFakeField(Method method, Class<? extends Annotation> annotation) {
        Annotation methodAnnotation = method.getAnnotation(annotation);
        if (methodAnnotation instanceof SelectProvider) {
            return Constants.FAKE_METHOD.equals(((SelectProvider) methodAnnotation).method());
        } else if (methodAnnotation instanceof InsertProvider) {
            return Constants.FAKE_METHOD.equals(((InsertProvider) methodAnnotation).method());
        } else if (methodAnnotation instanceof DeleteProvider) {
            return Constants.FAKE_METHOD.equals(((DeleteProvider) methodAnnotation).method());
        } else if (methodAnnotation instanceof UpdateProvider) {
            return Constants.FAKE_METHOD.equals(((UpdateProvider) methodAnnotation).method());
        }
        return false;
    }

    public void clearCache() {
        this.includeMethodNames.clear();
    }
}
