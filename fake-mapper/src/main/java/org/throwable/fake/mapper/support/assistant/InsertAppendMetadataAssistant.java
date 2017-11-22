package org.throwable.fake.mapper.support.assistant;

import org.throwable.fake.mapper.common.model.ColumnMetadata;

import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/18 11:29
 */
public class InsertAppendMetadataAssistant extends ConditionAppendMetadataAssistant {

	public static String insertIntoTable(Class<?> entityClass) {
		return "INSERT INTO " + fetchExistTableName(entityClass);
	}

	public static String insertIgnoreIntoTable(Class<?> entityClass) {
		return "INSERT IGNORE INTO " + fetchExistTableName(entityClass);
	}

	public static String insertColumns(Class<?> entityClass, boolean skipPrimaryKey, boolean skipNull) {
		Set<ColumnMetadata> columnMetadataSet = getExistInsertableColumnMetadata(entityClass, skipPrimaryKey);
		StringBuilder sql = new StringBuilder();
		sql.append("\n<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
		columnMetadataSet.forEach(metadata -> {
					if (skipNull || !metadata.isNullable() || metadata.isIdentity()) {
						if (String.class.equals(metadata.getJavaType())) {
							sql.append(metadata.getIfNotEmptyColumnByPropertyEqualHolderWithComma());
						} else {
							sql.append(metadata.getIfNotNullColumnByPropertyEqualHolderWithComma());
						}
					} else {
						sql.append(metadata.getColumnWithComma());
					}
				});
		sql.append("\n</trim>\n");
		return sql.toString();
	}

	public static String insertValues(Class<?> entityClass, boolean skipPrimaryKey, boolean skipNull) {
		Set<ColumnMetadata> columnMetadataSet = getExistInsertableColumnMetadata(entityClass, skipPrimaryKey);
		StringBuilder sql = new StringBuilder();
		sql.append("VALUES");
		sql.append("\n<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
		columnMetadataSet.forEach(metadata -> {
					if (skipNull || !metadata.isNullable() || metadata.isIdentity()) {
						if (String.class.equals(metadata.getJavaType())) {
							sql.append(metadata.getIfEmptyColumnPropertyHolderWithComma());
						} else {
							sql.append(metadata.getIfNullColumnPropertyHolderWithComma());
						}
					} else {
						sql.append(metadata.getColumnPropertyHolderWithComma());
					}
				});
		sql.append("\n</trim>\n");
		return sql.toString();
	}
}
