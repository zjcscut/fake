package org.throwable.fake.mapper.configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.throwable.fake.mapper.common.model.TableMetadata;
import org.throwable.fake.mapper.utils.AssertUtils;
import org.throwable.fake.mapper.utils.ReflectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 1:32
 */
public class FakeMapperRegistrar {

    private static final Log log = LogFactory.getLog(FakeMapperRegistrar.class);
    private final Map<String, MapperMethodMetadata> mapperMethodMetadata = Maps.newHashMap();
    private final List<MappedStatementRewriter> mappedStatementRewriters = Lists.newArrayList();
    private final Set<Class<?>> entityClasses = Sets.newHashSet();
    private Configuration configuration;
    private FakeMapperScanner fakeMapperScanner;
    private FakeMapperAnnotationParser fakeMapperAnnotationParser;

    public FakeMapperRegistrar(Configuration configuration, String[] packages) {
        AssertUtils.notNull(configuration, "[FakeMapperRegistrar] --> Configuration of mybatis must not be null!");
        AssertUtils.notEmpty(packages, "[FakeMapperRegistrar] --> mapper packages to scan must not be empty!");
        this.configuration = configuration;
        this.fakeMapperScanner = new FakeMapperScanner(packages);
        this.fakeMapperAnnotationParser = new FakeMapperAnnotationParser();
        this.mappedStatementRewriters.add(new DefaultMappedStatementRewriter());
    }

    public void registerMappers() {
        Set<Class<?>> mapperClasses = fakeMapperScanner.doScan();
        for (Class<?> mapperClass : mapperClasses) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Processing registering mapper --> %s", mapperClass.getCanonicalName()));
            }
            Set<MapperMethodMetadata> mapperMethodMetadata = fakeMapperAnnotationParser.doParse(mapperClass);
            mapperMethodMetadata.forEach(metadata -> this.mapperMethodMetadata.putIfAbsent(metadata.getMappedStatementId(), metadata));
        }
        registerMappedStatementEntity();
        registerEntityTableMetadata();
        rewriteMappedStatements();
        removeDuplicatedMappedStatements();
        clearCaches();
    }

    private void registerMappedStatementEntity() {
        for (Map.Entry<String, MapperMethodMetadata> entry : mapperMethodMetadata.entrySet()) {
            if (!StaticMapperRegistry.containsMappedStatementEntity(entry.getKey())) {
                StaticMapperRegistry.registerMappedStatementEntity(entry.getKey(), entry.getValue().getGenericClass());
                entityClasses.add(entry.getValue().getGenericClass());
            }
        }
    }

    private void registerEntityTableMetadata() {
        entityClasses.forEach(entityClass -> {
            TableMetadata tableMetadata = TableMetadataParser.processTableMetadata(entityClass);
            if (null != tableMetadata && !StaticMapperRegistry.containsTableMetadata(entityClass)) {
                StaticMapperRegistry.registerEntityTableMetadata(entityClass, tableMetadata);
            }
        });
    }

    private void rewriteMappedStatements() {
        StaticMapperRegistry.getAllRegisterMappedStatementId()
                .forEach(mappedStatementId -> {
                    MappedStatement mappedStatement = configuration.getMappedStatement(mappedStatementId);
                    if (null != mappedStatement) {
                        mappedStatementRewriters.forEach(rewriter ->
                                rewriter.rewriteMappedStatement(mappedStatement, mapperMethodMetadata.get(mappedStatement.getId())));
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private void removeDuplicatedMappedStatements() {
        try {
            ReflectionUtils.doInReflection(configuration, "mappedStatements",
                    (ReflectionUtils.FieldFunction<Configuration>) (field, configuration) -> {
                        Map<String, MappedStatement> mappedStatements = (Map<String, MappedStatement>) field.get(configuration);
                        fakeMapperAnnotationParser.getIncludeMethodNames().forEach(mappedStatements::remove);
                    });
        } catch (Exception e) {
            //ignore
        }
    }

    private void clearCaches() {
        StaticMapperRegistry.clearNonEssentialCaches();
        entityClasses.clear();
        mapperMethodMetadata.clear();
        mappedStatementRewriters.clear();
        fakeMapperAnnotationParser.clearCache();
    }

    public void registerMappedStatementRewriter(MappedStatementRewriter mappedStatementRewriter) {
        if (!this.mappedStatementRewriters.contains(mappedStatementRewriter)) {
            this.mappedStatementRewriters.add(mappedStatementRewriter);
        }
    }
}
