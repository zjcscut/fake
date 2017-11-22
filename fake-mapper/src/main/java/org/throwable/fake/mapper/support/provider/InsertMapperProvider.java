package org.throwable.fake.mapper.support.provider;

import org.apache.ibatis.mapping.MappedStatement;

import static org.throwable.fake.mapper.configuration.MybatisParseHelper.getExistEntityClassByMappedStatement;
import static org.throwable.fake.mapper.support.assistant.InsertAppendMetadataAssistant.*;


/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/16 23:28
 */
public class InsertMapperProvider extends AbstractMapperProvider {

    public String insertInternal(MappedStatement ms) {
        Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
        StringBuilder sql = new StringBuilder(insertIntoTable(entityClass));
        sql.append(insertColumns(entityClass,  false, false));
        sql.append(insertValues(entityClass, false, false));
        return sql.toString();
    }

    public String insertSkipNullInternal(MappedStatement ms) {
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder(insertIntoTable(entityClass));
		sql.append(insertColumns(entityClass,  false, true));
		sql.append(insertValues(entityClass, false, true));
		return sql.toString();
    }

    public String insertIgnoreInternal(MappedStatement ms) {
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder(insertIgnoreIntoTable(entityClass));
		sql.append(insertColumns(entityClass,  false, false));
		sql.append(insertValues(entityClass, false, false));
		return sql.toString();
    }

    public String insertIgnoreSkipNullInternal(MappedStatement ms) {
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder(insertIgnoreIntoTable(entityClass));
		sql.append(insertColumns(entityClass,  false, true));
		sql.append(insertValues(entityClass, false, true));
		return sql.toString();
    }
}
