package org.throwable.fake.mapper.support.assistant;

import org.throwable.fake.mapper.common.model.ColumnMetadata;

import java.util.Set;

import static org.throwable.fake.mapper.common.constant.Constants.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/19 22:00
 */
public class UpdateAppendMetadataAssistant extends ConditionAppendMetadataAssistant {

	public static String updateTable(Class<?> entityClass) {
		return "UPDATE " + fetchExistTableName(entityClass);
	}

	public static String updateSetColumns(Class<?> entityClass) {
		Set<ColumnMetadata> updatableColumns = getExistUpdatableColumnMetadata(entityClass, true);
		StringBuilder clause = new StringBuilder();
		clause.append("\n<trim suffixOverrides=\",\">\n<set>\n<choose>");
		clause.append(String.format("\n<when test=\"%s\">\n", PARAM_ALLOW_UPDATE_TO_NULL));   //allow update null column
		updatableColumns.forEach(metadata -> clause.append(metadata.getColumnPropertyEqualHolderWithComma(PARAM_RECORD)));
		clause.append("\n</when>");
		clause.append("\n<otherwise>");
		updatableColumns.forEach(metadata -> {
			if (String.class.equals(metadata.getJavaType())) {
				clause.append(metadata.getIfNotEmptyColumnPropertyEqualHolderWithComma(PARAM_RECORD));
			} else {
				clause.append(metadata.getIfNotNullColumnPropertyEqualHolderWithComma(PARAM_RECORD));
			}
		});
		clause.append("\n</otherwise>");
		clause.append("\n</choose>\n</set>\n</trim>");
		return clause.toString();
	}

	public static String primaryKeyWhereClause(Class<?> entityClass) {
		return String.format("\n<where>\n%s\n</where>\n",
				fetchExistPrimaryColumnMetadata(entityClass).getColumnPropertyEqualHolder(PARAM_RECORD));
	}

	public static String updateSetColumnsByCondition() {
		StringBuilder clause = new StringBuilder();
		clause.append("\n<trim suffixOverrides=\",\">\n<set>\n");
		clause.append(String.format("<foreach collection=\"%s\" item=\"%s\" index=\"%s\" separator=\",\" >\n",
				String.format("%s.%s", PARAM_CONDITION, PARAM_UPDATECOLUMNMAP),
				PARAM_VALUE, PARAM_COLUMN));
		clause.append(String.format("${%s} = #{%s},\n", PARAM_COLUMN, PARAM_VALUE)); //column must use '${}',value must use '#{}'
		clause.append("</foreach>");
		clause.append("\n</set>\n</trim>");
		return clause.toString();
	}
}
