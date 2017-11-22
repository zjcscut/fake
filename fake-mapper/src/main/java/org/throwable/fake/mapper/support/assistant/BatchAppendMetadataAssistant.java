package org.throwable.fake.mapper.support.assistant;

import org.throwable.fake.mapper.common.model.ColumnMetadata;
import org.throwable.fake.mapper.support.ognl.OGNL;

import java.util.Set;

import static org.throwable.fake.mapper.common.constant.Constants.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/26 11:13
 */
public class BatchAppendMetadataAssistant extends ConditionAppendMetadataAssistant {

	public static String checkListNotEmpty() {
		return getTestClause(String.format(OGNL.CHECK_RECORDS_NOT_EMPTY, PARAM_LIST));
	}

	public static String checkRecordsNotEmpty() {
		return getTestClause(String.format(OGNL.CHECK_RECORDS_NOT_EMPTY, PARAM_RECORDS));
	}

	public static String insertBatchColumns(Class<?> entityClass) {
		Set<ColumnMetadata> columnMetadataSet = getExistInsertableColumnMetadata(entityClass, false);
		return insertBatchColumnsPair(columnMetadataSet);
	}

	private static String insertBatchColumnsPair(Set<ColumnMetadata> columnMetadataSet) {
		StringBuilder sql = new StringBuilder();
		sql.append("\n<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
		columnMetadataSet.forEach(metadata -> sql.append(metadata.getColumn()).append(COMMA));
		sql.append("\n</trim>\n");
		return sql.toString();
	}

	public static String insertBatchValues(Class<?> entityClass) {
		Set<ColumnMetadata> columnMetadataSet = getExistInsertableColumnMetadata(entityClass, false);
		return insertBatchValuesPair(columnMetadataSet);
	}

	private static String insertBatchValuesPair(Set<ColumnMetadata> columnMetadataSet) {
		StringBuilder sql = new StringBuilder();
		sql.append(" VALUES ");
		sql.append(String.format("\n<foreach collection=\"%s\" item=\"%s\" separator=\",\" >\n", PARAM_LIST, PARAM_RECORD));
		sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
		columnMetadataSet.forEach(metadata -> sql.append(metadata.getColumnPropertyHolderWithComma(PARAM_RECORD)));
		sql.append("\n</trim>\n");
		sql.append("</foreach>\n");
		return sql.toString();
	}

	public static String batchUpdateForeachColumns(Class<?> entityClass) {
		Set<ColumnMetadata> columnMetadataSet = getExistUpdatableColumnMetadata(entityClass, true);
		ColumnMetadata keyColumn = fetchExistPrimaryColumnMetadata(entityClass);
		String tableName = fetchExistTableName(entityClass);
		return batchUpdateForeachColumnsPair(columnMetadataSet, tableName, keyColumn);
	}

	private static String batchUpdateForeachColumnsPair(Set<ColumnMetadata> updatableColumns, String tableName, ColumnMetadata keyColumn) {
		StringBuilder clause = new StringBuilder();
		clause.append(String.format("<foreach collection=\"%s\" item=\"%s\" separator=\";\" >\n", PARAM_RECORDS, PARAM_RECORD));
		clause.append("UPDATE ").append(tableName).append("\n");
		clause.append("<trim suffixOverrides=\",\">\n<set>\n<choose>");
		clause.append(String.format("\n<when test=\"%s\">", SKIP_NULL));   //allow update null column
		updatableColumns.forEach(metadata -> {
			clause.append("\n");
			if (String.class.equals(metadata.getJavaType())) {
				clause.append(metadata.getIfNotEmptyColumnPropertyEqualHolderWithComma(PARAM_RECORD));
			} else {
				clause.append(metadata.getIfNotNullColumnPropertyEqualHolderWithComma(PARAM_RECORD));
			}
			clause.append("\n");
		});
		clause.append("</when>");
		clause.append("\n<otherwise>");
		updatableColumns.forEach(metadata -> clause.append("\n").append(metadata.getColumnPropertyEqualHolderWithComma(PARAM_RECORD)).append("\n"));
		clause.append("</otherwise>");
		clause.append("\n</choose>\n</set>\n</trim>\n");
		clause.append("<where>\n");
		clause.append(keyColumn.getColumnPropertyEqualHolder(PARAM_RECORD));
		clause.append("\n</where>");
		clause.append("\n</foreach>\n");
		return clause.toString();
	}

	public static String batchUpdateWhenCaseColumns(Class<?> entityClass) {
		Set<ColumnMetadata> columnMetadataSet = getExistUpdatableColumnMetadata(entityClass, true);
		ColumnMetadata keyColumn = fetchExistPrimaryColumnMetadata(entityClass);
		String tableName = fetchExistTableName(entityClass);
		return batchUpdateWhenCaseColumnsPair(columnMetadataSet, tableName, keyColumn) +
				batchUpdateWhenCasePrimaryKeyWhereClause(keyColumn);
	}

	private static String batchUpdateWhenCaseColumnsPair(Set<ColumnMetadata> updatableColumns, String tableName, ColumnMetadata keyColumn) {
		StringBuilder clause = new StringBuilder();
		clause.append("UPDATE ").append(tableName).append("\n");
		clause.append("<trim prefix=\"set\" suffixOverrides=\",\">\n");
		clause.append("<choose>");
		clause.append(String.format("\n<when test=\"%s\">\n", SKIP_NULL));   //allow update null column
		updatableColumns.forEach(metadata -> {
			clause.append(String.format("<trim prefix=\"%s = CASE %s\" suffix=\"END,\">\n", metadata.getColumn(), keyColumn.getColumn()));
			clause.append(String.format("<foreach collection=\"%s\" item=\"%s\">\n", PARAM_RECORDS, PARAM_RECORD));
			clause.append(getIfNotNull(String.format("%s.%s", PARAM_RECORD, metadata.getProperty()),
					String.format("WHEN %s THEN %s",
							keyColumn.getColumnPropertyHolder(PARAM_RECORD),
							metadata.getColumnPropertyHolder(PARAM_RECORD))));
			clause.append("\n</foreach>");
			clause.append("\n</trim>");
		});
		clause.append("\n</when>");
		clause.append("\n<otherwise>\n");
		updatableColumns.forEach(metadata -> {
			clause.append(String.format("\n<trim prefix=\"%s = CASE %s\" suffix=\"END,\">\n", metadata.getColumn(), keyColumn.getColumn()));
			clause.append(String.format("<foreach collection=\"%s\" item=\"%s\">\n", PARAM_RECORDS, PARAM_RECORD));
			clause.append(String.format("WHEN %s THEN %s",
					keyColumn.getColumnPropertyHolder(PARAM_RECORD),
					metadata.getColumnPropertyHolder(PARAM_RECORD)));
			clause.append("\n</foreach>");
			clause.append("\n</trim>\n");
		});
		clause.append("\n</otherwise>");
		clause.append("\n</choose>");
		clause.append("\n</trim>\n");
		return clause.toString();
	}

	private static String batchUpdateWhenCasePrimaryKeyWhereClause(ColumnMetadata keyColumn) {
		StringBuilder clause = new StringBuilder();
		clause.append("<where>\n");
		clause.append(keyColumn.getColumn()).append(" IN ");
		clause.append(String.format("\n<foreach collection=\"%s\" open=\"(\" close=\")\" item=\"%s\" separator=\",\" >\n", PARAM_RECORDS, PARAM_RECORD));
		clause.append(keyColumn.getIfNullColumnPropertyHolder(PARAM_RECORD));
		clause.append("\n</foreach>\n");
		clause.append("</where>\n");
		return clause.toString();
	}
}
