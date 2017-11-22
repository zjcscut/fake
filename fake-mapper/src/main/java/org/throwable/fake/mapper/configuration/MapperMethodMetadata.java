package org.throwable.fake.mapper.configuration;

import lombok.Data;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.mapping.SqlCommandType;
import org.throwable.fake.mapper.support.provider.NoneProvider;
import org.throwable.fake.mapper.utils.AssertUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/16 23:59
 */
@Data
public class MapperMethodMetadata {

    private Annotation[] annotations;
    private Method method;
    private String methodName;
    private String providerMethodName;
    private Class<?> mapperClass;
    private Class<?> resultType;
    private Class<?>[] parameterTypes;
    private String mappedStatementId;
    private SqlCommandType sqlCommandType;
    private Class<?> providerClass;
    private Class<?> primaryKeyClass;
    private Class<?> genericClass;

    public MapperMethodMetadata(Method method, Class<?> mapperClass,Class<?> primaryKeyClass, Class<?> genericClass) {
        AssertUtils.notNull(method, "method to create MapperMethodMetadata must not be null!");
        AssertUtils.notNull(mapperClass, "mapperClass to create MapperMethodMetadata must not be null!");
        this.method = method;
        this.annotations = method.getAnnotations();
        this.methodName = method.getName();
        this.mapperClass = mapperClass;
        this.resultType = method.getReturnType();
        this.parameterTypes = method.getParameterTypes();
        this.mappedStatementId = mapperClass.getName() + "." + methodName;
        this.providerMethodName = methodName;
        this.primaryKeyClass = primaryKeyClass;
        this.genericClass = genericClass;
        processSqlCommandType();
    }

    private void processSqlCommandType() {
        this.sqlCommandType = SqlCommandType.UNKNOWN;
        this.providerClass = NoneProvider.class;
        if (null != annotations && annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof SelectProvider) {
                    this.sqlCommandType = SqlCommandType.SELECT;
                    this.providerClass = ((SelectProvider) annotation).type();
                } else if (annotation instanceof InsertProvider) {
                    this.sqlCommandType = SqlCommandType.INSERT;
                    this.providerClass = ((InsertProvider) annotation).type();
                } else if (annotation instanceof UpdateProvider) {
                    this.sqlCommandType = SqlCommandType.UPDATE;
                    this.providerClass = ((UpdateProvider) annotation).type();
                } else if (annotation instanceof DeleteProvider) {
                    this.sqlCommandType = SqlCommandType.DELETE;
                    this.providerClass = ((DeleteProvider) annotation).type();
                }
            }
        }
    }
}
