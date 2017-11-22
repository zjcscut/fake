package org.throwable.fake.mapper.support.provider;

import org.apache.ibatis.mapping.MappedStatement;

import static org.throwable.fake.mapper.configuration.MybatisParseHelper.getExistEntityClassByMappedStatement;
import static org.throwable.fake.mapper.support.assistant.DeleteAppendMetadataAssistant.*;
import static org.throwable.fake.mapper.support.assistant.SelectAppendMetadataAssistant.conditionWhereClause;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/20 2:19
 */
public class DeleteMapperProvider extends AbstractMapperProvider {

	public String deleteByPrimaryKey(MappedStatement ms){
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder();
        sql.append(checkPrimaryKeyValid());
        sql.append(deleteFromTable(entityClass));
        sql.append(primaryKeyWhereClause(entityClass));
		return sql.toString();
	}

	public String deleteByCondition(MappedStatement ms){
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder();
		sql.append(deleteFromTable(entityClass));
		sql.append(conditionWhereClause());
		return sql.toString();
	}
}
