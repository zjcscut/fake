package org.throwable.fake.mapper.support.assistant;

import org.throwable.fake.mapper.common.model.ColumnMetadata;
import org.throwable.fake.mapper.support.ognl.OGNL;

import static org.throwable.fake.mapper.common.constant.Constants.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/19 22:00
 */
public class SelectAppendMetadataAssistant extends ConditionAppendMetadataAssistant {

	public static String selectColumnsForCondition(Class<?> entityClass) {
		return String.format("SELECT <if test=\"%s neq null and %sisDistinct\">DISTINCT</if>" +
						"\n" +
						"<if test=\"%s neq null and %s\">" +
						"\n<foreach collection=\"condition.selectColumns\" item=\"selectColumn\" separator=\",\">\n" +
						"    ${selectColumn}" +
						"\n</foreach>\n" +
						"</if>\n" +
						"<if test=\"%s eq null or %s\">\n" +
						getSelectColumnsClause(entityClass) +
						"\n</if>\n",
				getParameterPrefix(PARAM_CONDITION),
				getParameterPrefixWithDot(PARAM_CONDITION),
				getParameterPrefix(PARAM_CONDITION),
				String.format(OGNL.HAS_CONDITION_SELECT_COLUMNS, getParameterPrefix(PARAM_CONDITION)),
				getParameterPrefix(PARAM_CONDITION),
				String.format(OGNL.HAS_NONE_CONDITION_SELECT_COLUMNS, getParameterPrefix(PARAM_CONDITION)));
	}

	public static String fromTable(Class<?> entityClass) {
		return " FROM " + fetchExistTableName(entityClass);
	}

	public static String getSelectColumnsClause(Class<?> entityClass) {
		return getAllExistColumnMetadata(entityClass)
				.stream()
				.map(ColumnMetadata::getColumn)
				.reduce((column1, column2) -> column2 + "," + column1)
				.orElse("");
	}

	public static String conditionWhereClause(){
		return conditionWhereClause(PARAM_CONDITION);
	}

	public static String conditionOrderByClause(){
		return conditionOrderByClause(PARAM_CONDITION);
	}

	public static String conditionLimitClause(){
		return conditionLimitClause(PARAM_CONDITION.concat(DOT).concat(PARAM_LIMIT));
	}

	public static String conditionOffsetClause(){
		return conditionOffsetClause(PARAM_CONDITION.concat(DOT).concat(PARAM_LIMIT));
	}

	public static String selectCountStar(){
		return "SELECT COUNT(*)";
	}
}
