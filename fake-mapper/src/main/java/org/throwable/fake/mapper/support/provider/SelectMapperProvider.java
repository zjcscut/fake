package org.throwable.fake.mapper.support.provider;

import org.apache.ibatis.mapping.MappedStatement;

import static org.throwable.fake.mapper.configuration.MybatisParseHelper.getExistEntityClassByMappedStatement;
import static org.throwable.fake.mapper.support.assistant.SelectAppendMetadataAssistant.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 17:02
 */
public class SelectMapperProvider extends AbstractMapperProvider {

	public String selectByCondition(MappedStatement ms) {
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder();
		sql.append(selectColumnsForCondition(entityClass));
		sql.append(fromTable(entityClass));
		sql.append(conditionWhereClause());
		sql.append(conditionOrderByClause());
		sql.append(conditionLimitClause());
		return sql.toString();
	}

	public String selectByConditionLimitOffset(MappedStatement ms){
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder();
		sql.append(selectColumnsForCondition(entityClass));
		sql.append(fromTable(entityClass));
		sql.append(conditionWhereClause());
		sql.append(conditionOrderByClause());
		sql.append(conditionOffsetClause());
		return sql.toString();
	}

	public String countByCondition(MappedStatement ms) {
		Class<?> entityClass = getExistEntityClassByMappedStatement(ms);
		StringBuilder sql = new StringBuilder();
		sql.append(selectCountStar());
		sql.append(fromTable(entityClass));
		sql.append(conditionWhereClause());
		sql.append(conditionOrderByClause());
		sql.append(conditionLimitClause());
		return sql.toString();
	}
}
