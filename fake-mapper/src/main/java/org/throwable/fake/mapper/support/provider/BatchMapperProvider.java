package org.throwable.fake.mapper.support.provider;

import org.apache.ibatis.mapping.MappedStatement;

import static org.throwable.fake.mapper.configuration.MybatisParseHelper.getExistEntityClassByMappedStatement;
import static org.throwable.fake.mapper.support.assistant.BatchAppendMetadataAssistant.*;
import static org.throwable.fake.mapper.support.assistant.InsertAppendMetadataAssistant.insertIntoTable;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/23 11:56
 */
public class BatchMapperProvider extends AbstractMapperProvider{

    public String batchInsertInternal(MappedStatement ms){
        Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(checkListNotEmpty());
        sql.append(insertIntoTable(entityClass));
        sql.append(insertBatchColumns(entityClass));
        sql.append(insertBatchValues(entityClass));
        return sql.toString();
    }

    public String batchUpdateByPrimaryKeyForeach(MappedStatement ms){
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder();
		sql.append(checkRecordsNotEmpty());
		sql.append(batchUpdateForeachColumns(entityClass));
        return sql.toString();
    }

    public String batchUpdateByPrimaryKeyWhenCase(MappedStatement ms){
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder();
		sql.append(checkRecordsNotEmpty());
		sql.append(batchUpdateWhenCaseColumns(entityClass));
		return sql.toString();
    }
}
