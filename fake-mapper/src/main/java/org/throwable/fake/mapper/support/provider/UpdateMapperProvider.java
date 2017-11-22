package org.throwable.fake.mapper.support.provider;

import org.apache.ibatis.mapping.MappedStatement;

import static org.throwable.fake.mapper.configuration.MybatisParseHelper.getExistEntityClassByMappedStatement;
import static org.throwable.fake.mapper.support.assistant.SelectAppendMetadataAssistant.conditionWhereClause;
import static org.throwable.fake.mapper.support.assistant.UpdateAppendMetadataAssistant.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/19 23:59
 */
public class UpdateMapperProvider extends AbstractMapperProvider {

	public String updateByPrimaryKey(MappedStatement ms) {
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder();
		sql.append(updateTable(entityClass));
		sql.append(updateSetColumns(entityClass));
		sql.append(primaryKeyWhereClause(entityClass));
		return sql.toString();
	}

	public String updateByCondition(MappedStatement ms){
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder();
		sql.append(updateTable(entityClass));
		sql.append(updateSetColumnsByCondition());
		sql.append(conditionWhereClause());
		return sql.toString();
	}
}
