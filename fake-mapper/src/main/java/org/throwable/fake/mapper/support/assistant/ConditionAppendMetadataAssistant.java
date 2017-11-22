package org.throwable.fake.mapper.support.assistant;

import org.throwable.fake.mapper.support.ognl.OGNL;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/18 10:57
 */
public abstract class ConditionAppendMetadataAssistant extends FieldFilterAssistant {

	public static String conditionWhereClause(String parameterName) {
		String conditionEntity = getParameterPrefix(parameterName);
		String conditionEntityWithDot = getParameterPrefixWithDot(parameterName);
		return String.format(
				"\n<where>\n" +
						"  <if test=\"%s neq null\">\n" +
						"    <foreach collection=\"%scriteriaCollection\" item=\"criteriaCollectionItem\" separator=\"or\">\n" +
						"     <if test=\"criteriaCollectionItem.valid\">\n" +
						"        <trim prefix=\"(\" prefixOverrides=\"and\" suffix=\")\">\n" +
						"          <foreach collection=\"criteriaCollectionItem.criteriaLinkedList\" item=\"criterion\">\n" +
						"            <choose>\n" +
						"              <when test=\"criterion.noneValue\">\n" +
						"                and ${criterion.conditionClause}\n" +
						"              </when>\n" +
						"              <when test=\"criterion.singleValue\">\n" +
						"                and ${criterion.conditionClause} #{criterion.leftValue}\n" +
						"              </when>\n" +
						"              <when test=\"criterion.betweenValue\">\n" +
						"                and ${criterion.conditionClause} #{criterion.leftValue} and #{criterion.rightValue}\n" +
						"              </when>\n" +
						"              <when test=\"criterion.collectionValue\">\n" +
						"                and ${criterion.conditionClause}\n" +
						"                <foreach close=\")\" collection=\"criterion.leftValue\" item=\"collectionItem\" open=\"(\" separator=\",\">\n" +
						"                  #{collectionItem}\n" +
						"                </foreach>\n" +
						"              </when>\n" +
						"            </choose>\n" +
						"          </foreach>\n" +
						"        </trim>\n" +
						"      </if>\n" +
						"    </foreach>\n" +
						"  </if>\n" +
						"</where>\n", conditionEntity, conditionEntityWithDot);
	}

	public static String conditionOrderByClause(String parameterName) {
		String conditionEntity = getParameterPrefix(parameterName);
		String conditionEntityWithDot = getParameterPrefixWithDot(parameterName);
		return String.format(
				"<if test=\"%s neq null and %s\"> \n" +
						"<trim prefix=\"ORDER BY\">\n" +
						"  <foreach collection=\"%ssort.orders\" item=\"order\" separator=\", \">\n" +
						"    ${order.property} ${order.direction}" +
						"  \n</foreach>\n" +
						"</trim>\n" +
						"</if>\n",
				conditionEntity,
				String.format(OGNL.HAS_CONDITION_ORDER_BY_CLAUSE, conditionEntity),
				conditionEntityWithDot);
	}

	public static String conditionLimitClause(String parameterName) {
		String conditionEntity = getParameterPrefix(parameterName);
		String conditionEntityWithDot = getParameterPrefixWithDot(parameterName);
		return getIfNotNull(conditionEntity, String.format(" LIMIT #{%soffset}, #{%ssize}", conditionEntityWithDot, conditionEntityWithDot));
	}

	public static String conditionOffsetClause(String parameterName){
		String conditionEntity = getParameterPrefix(parameterName);
		String conditionEntityWithDot = getParameterPrefixWithDot(parameterName);
		return getIfNotNull(conditionEntity, String.format(" OFFSET #{%soffset} LIMIT #{%ssize}", conditionEntityWithDot, conditionEntityWithDot));
	}
}
