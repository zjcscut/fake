package org.throwable.fake.mapper.configuration;

import com.google.common.collect.Maps;
import org.throwable.fake.mapper.common.model.TableMetadata;

import java.util.Map;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 1:32
 */
public abstract class StaticMapperRegistry {

    /**
     * mappedStatementId -> entityClass
     */
    private static final Map<String, Class<?>> MAPPED_STATEMENT_ENTITY = Maps.newConcurrentMap();

    /**
     * entityClass -> tableMetadata (condition plugins depend on this,must not be cleared)
     */
    private static final Map<Class<?>, TableMetadata> ENTITY_TABLE_METADATA = Maps.newConcurrentMap();
    /**
     * mappedStatementId -> scriptSql (clear after loading all mappers)
     */
    private static final Map<String, String> MAPPED_STATEMENT_SCRIPT_SQL = Maps.newConcurrentMap();

    public static boolean registerMappedStatementEntity(String mappedStatementId, Class<?> entityClass) {
        return null != MAPPED_STATEMENT_ENTITY.putIfAbsent(mappedStatementId, entityClass);
    }

    public static Class<?> getEntityClassByMappedStatementId(String mappedStatementId) {
        return MAPPED_STATEMENT_ENTITY.get(mappedStatementId);
    }

    public static boolean containsMappedStatementEntity(String mappedStatementId) {
        return MAPPED_STATEMENT_ENTITY.containsKey(mappedStatementId);
    }

    public static Set<String> getAllRegisterMappedStatementId() {
        return MAPPED_STATEMENT_ENTITY.keySet();
    }

    public static boolean registerEntityTableMetadata(Class<?> entityClass, TableMetadata tableMetadata) {
        return null != ENTITY_TABLE_METADATA.putIfAbsent(entityClass, tableMetadata);
    }

    public static TableMetadata getTableMetadataByEntityClass(Class<?> entityClass) {
        return ENTITY_TABLE_METADATA.get(entityClass);
    }

    public static boolean containsTableMetadata(Class<?> entityClass) {
        return ENTITY_TABLE_METADATA.containsKey(entityClass);
    }

    public static boolean registerScriptSql(String mappedStatementId, String scriptSql) {
        return null != MAPPED_STATEMENT_SCRIPT_SQL.putIfAbsent(mappedStatementId, scriptSql);
    }

    public static String getScriptSql(String mappedStatementId) {
        return MAPPED_STATEMENT_SCRIPT_SQL.get(mappedStatementId);
    }

    public static boolean containsScriptSql(String mappedStatementId) {
        return MAPPED_STATEMENT_SCRIPT_SQL.containsKey(mappedStatementId);
    }

    public static void clearNonEssentialCaches() {
        MAPPED_STATEMENT_SCRIPT_SQL.clear();
    }
}
