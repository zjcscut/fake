package org.throwable.fake.mapper.configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.throwable.fake.mapper.Mapper;
import org.throwable.fake.mapper.common.model.ColumnMetadata;
import org.throwable.fake.mapper.common.model.TableMetadata;
import org.throwable.fake.mapper.exception.MapperRegisterException;
import org.throwable.fake.mapper.exception.InvalidMapperProviderException;
import org.throwable.fake.mapper.support.assistant.SqlAppendMetadataAssistant;
import org.throwable.fake.mapper.support.provider.AbstractMapperProvider;
import org.throwable.fake.mapper.support.provider.NoneProvider;
import org.throwable.fake.mapper.utils.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 15:20
 */
public class DefaultMappedStatementRewriter implements MappedStatementRewriter {

    private static final Map<Class<?>, Object> PROVIDERS = Maps.newHashMap();
    private static final XMLLanguageDriver DRIVER = new XMLLanguageDriver();

    @Override
    public void rewriteMappedStatement(MappedStatement mappedStatement, MapperMethodMetadata mapperMethodMetadata) {
        if (!checkMapperTypeValid(mappedStatement, mapperMethodMetadata)) {
            return;
        }
        if (!checkProviderClassValid(mapperMethodMetadata.getProviderClass())) {
            throw new InvalidMapperProviderException(String.format("Invalid mapper provider class:[%s],target class must implements AbstractMapperProvider",
                    mapperMethodMetadata.getProviderClass().getName()));
        }
        createProviderInstanceIfNecessary(mapperMethodMetadata.getProviderClass());
        Object provider = PROVIDERS.get(mapperMethodMetadata.getProviderClass());
        try {
            MetaObject metaObject = SystemMetaObject.forObject(mappedStatement);
            Method methodToInvoke = fetchProviderTargetMethod(mapperMethodMetadata.getProviderClass(),
                    mapperMethodMetadata.getProviderMethodName());
            if (!checkProviderMethodValid(methodToInvoke, mapperMethodMetadata)) {
                throw new InvalidMapperProviderException(String.format("Invalid mapper method [%s] for provider [%s],method modifier:[%s],only supports public method!",
                        methodToInvoke.getName(), mapperMethodMetadata.getProviderClass().getName(), Modifier.toString(methodToInvoke.getModifiers())));
            }
            if (methodToInvoke.getReturnType().equals(Void.TYPE)) {
                methodToInvoke.invoke(provider, mappedStatement);
            } else if (String.class.equals(methodToInvoke.getReturnType())) {
                String sql = (String) methodToInvoke.invoke(provider, mappedStatement);
                registerMappedStatementScriptSql(mappedStatement, sql);
                SqlSource sqlSource = createXmlScriptSqlSource(mappedStatement, sql);
                if (metaObject.hasSetter("sqlSource")) {
                    metaObject.setValue("sqlSource", sqlSource);
                }
            }
            processMappedStatementFields(metaObject, mappedStatement, mapperMethodMetadata);
        } catch (Exception e) {
            throw new MapperRegisterException(String.format("Rewrite mappedStatement [%s] failed!",
                    mappedStatement.getId()), e);
        }
    }

    private boolean checkMapperTypeValid(MappedStatement mappedStatement, MapperMethodMetadata mapperMethodMetadata) {
        String mapperClassName = MybatisParseHelper.getMappedStatementNameSpace(mappedStatement);
        if (!mapperClassName.equals(mapperMethodMetadata.getMapperClass().getName())) {
            return false;
        }
        try {
            Class<?> mapperClass = Class.forName(mapperClassName);
            if (!Mapper.class.isAssignableFrom(mapperClass)) {
                return false;
            }
        } catch (ClassNotFoundException e) {
            //ignore exception
            return false;
        }
        return true;
    }

    private boolean checkProviderClassValid(Class<?> providerClass) {
        return !(null == providerClass || NoneProvider.class.isAssignableFrom(providerClass)) &&
                AbstractMapperProvider.class.isAssignableFrom(providerClass);
    }

    private boolean checkProviderMethodValid(Method method, MapperMethodMetadata mapperMethodMetadata) {
        if (null == method) {
            throw new IllegalArgumentException(String.format("Method [%s] for provider class [%s] could not be found!",
                    mapperMethodMetadata.getProviderMethodName(), mapperMethodMetadata.getProviderClass().getName()));
        }
        return Modifier.isPublic(method.getModifiers())
                && !Modifier.isAbstract(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers());
    }

    private void createProviderInstanceIfNecessary(Class<?> providerClass) {
        try {
            Class<?> clazz = Class.forName(providerClass.getName());
            PROVIDERS.putIfAbsent(providerClass, clazz.newInstance());
        } catch (Exception e) {
            throw new MapperRegisterException(String.format("Create provider instance failed,provider class:[%s]",
                    providerClass.getName()), e);
        }
    }

    private Method fetchProviderTargetMethod(Class<?> providerClass, String providerMethodName) {
        try {
            return providerClass.getDeclaredMethod(providerMethodName, MappedStatement.class);
        } catch (NoSuchMethodException e) {
            throw new MapperRegisterException(String.format("Fetch target method from provider failed," +
                    "method:[%s],providerClass:[%s]", providerMethodName, providerClass.getName()), e);
        }
    }

    private SqlSource createXmlScriptSqlSource(MappedStatement mappedStatement, String xmlSql) {
        return DRIVER.createSqlSource(mappedStatement.getConfiguration(), "<script>\n\t" + xmlSql + "</script>", null);
    }

    private void processMappedStatementFields(MetaObject metaObject,
                                              MappedStatement mappedStatement,
                                              MapperMethodMetadata mapperMethodMetadata) throws NoSuchFieldException, IllegalAccessException {
        ColumnMetadata keyColumnMetadata
                = SqlAppendMetadataAssistant.fetchPrimaryColumnMetadata(mapperMethodMetadata.getGenericClass());
        if (null == keyColumnMetadata) {
            throw new MapperRegisterException(String.format("Primary key field for entity [%s] is not found!",
                    mapperMethodMetadata.getGenericClass().getCanonicalName()));
        }
        if (!keyColumnMetadata.getJavaType().equals(mapperMethodMetadata.getPrimaryKeyClass())) {
            throw new MapperRegisterException(String.format("Primary key field class for entity [%s] is not match to mapper [%s] primary key class!",
                    mapperMethodMetadata.getGenericClass().getCanonicalName(), mapperMethodMetadata.getMapperClass().getCanonicalName()));
        }
        //process keyGenerator
        if (SqlCommandType.INSERT.equals(mapperMethodMetadata.getSqlCommandType())) {
            if (metaObject.hasSetter("keyGenerator")) {
                metaObject.setValue("keyGenerator", new Jdbc3KeyGenerator()); //#since mybatis 3.4.3
            }
            //process key properties and columns
            String[] keyPropertiesArray = {keyColumnMetadata.getProperty()};
            metaObject.setValue("keyProperties", keyPropertiesArray);
            String[] keyColumnsArray = {keyColumnMetadata.getColumn()};
            metaObject.setValue("keyColumns", keyColumnsArray);
        }
        TableMetadata tableMetadata
                = StaticMapperRegistry.getTableMetadataByEntityClass(mapperMethodMetadata.getGenericClass());
        if (null != tableMetadata) {
            //process resultMap,only in select mode
            if (!mappedStatement.hasNestedResultMaps() && SqlCommandType.SELECT.equals(mapperMethodMetadata.getSqlCommandType())) {
                if (mapperMethodMetadata.getResultType().isPrimitive()
                        || ReflectionUtils.isPrimitiveWrappedClass(mapperMethodMetadata.getResultType())) { //process primitive and wrapped primitive return type
                    List<ResultMap> resultMaps = Lists.newArrayList();
                    ResultMap inlineResultMap = new ResultMap.Builder(mappedStatement.getConfiguration(),
                            mappedStatement.getId() + "-Inline",
                            mapperMethodMetadata.getResultType(),
                            new ArrayList<>(),
                            null).build();
                    resultMaps.add(inlineResultMap);
                    metaObject.setValue("resultMaps", resultMaps);
                } else { //process none primitive return type
                    List<ResultMap> resultMapList = Lists.newArrayList(tableMetadata.createBaseResultMap(mappedStatement.getConfiguration()));
                    metaObject.setValue("resultMaps", resultMapList);
                }
            }
        }
        processBatchInsertParameterType(metaObject, mappedStatement, mapperMethodMetadata);
        checkAndProcessCache(metaObject, mappedStatement);
    }

    private void checkAndProcessCache(MetaObject metaObject,
                                      MappedStatement mappedStatement) {
        Cache cache = mappedStatement.getCache();
        if (null == cache) {
            String nameSpace = MybatisParseHelper.getMappedStatementNameSpace(mappedStatement);
            try {
                cache = mappedStatement.getConfiguration().getCache(nameSpace);
            } catch (IllegalArgumentException e) {
                //ignore
            }
        }
        if (null != cache) {
            if (metaObject.hasSetter("cache")) {
                metaObject.setValue("cache", cache);
            }
        }
    }

    private void registerMappedStatementScriptSql(MappedStatement mappedStatement, String sql) {
        if (!StaticMapperRegistry.containsScriptSql(mappedStatement.getId())) {
            StaticMapperRegistry.registerScriptSql(mappedStatement.getId(), sql);
        }
    }

    private void processBatchInsertParameterType(MetaObject metaObject, MappedStatement mappedStatement, MapperMethodMetadata mapperMethodMetadata) {
        if ("batchInsertInternal".equals(mapperMethodMetadata.getMethodName())) {
            ParameterMap.Builder builder = new ParameterMap.Builder(mappedStatement.getConfiguration(),
                    String.format("%s.%s", mappedStatement.getId(), "batchInsertInternalParameterType"), List.class, Lists.newArrayList());
            metaObject.setValue("parameterMap", builder.build());
        }
    }
}
