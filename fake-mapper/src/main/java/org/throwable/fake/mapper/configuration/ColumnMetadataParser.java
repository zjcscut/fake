package org.throwable.fake.mapper.configuration;

import org.apache.ibatis.type.JdbcType;
import org.throwable.fake.mapper.common.annotation.Column;
import org.throwable.fake.mapper.common.annotation.Id;
import org.throwable.fake.mapper.common.model.ColumnMetadata;
import org.throwable.fake.mapper.common.model.FieldMetadata;
import org.throwable.fake.mapper.common.model.TableMetadata;
import org.throwable.fake.mapper.exception.MetadataParseException;
import org.throwable.fake.mapper.exception.UnsupportedKeyGeneratorException;
import org.throwable.fake.mapper.exception.UnsupportedKeyTypeException;
import org.throwable.fake.mapper.support.plugins.generator.identity.NonePrimaryKeyGenerator;
import org.throwable.fake.mapper.support.plugins.generator.identity.PrimaryKeyChecker;
import org.throwable.fake.mapper.support.plugins.generator.identity.PrimaryKeyGeneratorRepository;
import org.throwable.fake.mapper.support.plugins.generator.type.NoneTypeHandler;
import org.throwable.fake.mapper.utils.StringUtils;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 0:48
 */
public abstract class ColumnMetadataParser {

	public static void processColumnMetadata(TableMetadata tableMetadata, FieldMetadata fieldMetadata) {
		ColumnMetadata columnMetadata = new ColumnMetadata(tableMetadata);
		if (fieldMetadata.isAnnotationPresent(Id.class) && fieldMetadata.isAnnotationPresent(Column.class)) {
			throw new MetadataParseException(String.format("@Id and @Column can not be used on the same field!" +
					"Entity:[%s],property:[%s]", tableMetadata.getEntityClass().getName(), fieldMetadata.getName()));
		}
		columnMetadata.setProperty(fieldMetadata.getName());
		columnMetadata.setJavaType(fieldMetadata.getJavaType());
		//@Id
		if (fieldMetadata.isAnnotationPresent(Id.class)) {
			Id id = fieldMetadata.getAnnotation(Id.class);
			columnMetadata.setIdentity(true);
			columnMetadata.setAutoIncrement(id.autoIncrement());
			if (StringUtils.isNotBlank(id.value())) {
				columnMetadata.setColumn(id.value());
			} else {
				columnMetadata.setColumn(fieldMetadata.getName());
			}
			checkPrimaryKeyTypeValid(columnMetadata.getJavaType(), tableMetadata.getEntityClass());
			processPrimaryKeyGenerator(id, fieldMetadata, columnMetadata);
		}
		//@Column
		if (fieldMetadata.isAnnotationPresent(Column.class)) {
			Column column = fieldMetadata.getAnnotation(Column.class);
			if (StringUtils.isNotBlank(column.value())) {
				columnMetadata.setColumn(column.value());
			} else {
				columnMetadata.setColumn(fieldMetadata.getName());
			}
			columnMetadata.setInsertable(column.insertable());
			columnMetadata.setUpdatable(column.updatable());
			columnMetadata.setNullable(column.nullable());
			if (!JdbcType.UNDEFINED.equals(column.jdbcType())) {
				columnMetadata.setJdbcType(column.jdbcType());
			}
			if (!NoneTypeHandler.class.getName().equals(column.typeHandler().getName())) {
				columnMetadata.setTypeHandler(column.typeHandler());
			}
		}
		//@Id and @Column are not be annotated,use property for column
		if (StringUtils.isBlank(columnMetadata.getColumn())) {
			columnMetadata.setColumn(columnMetadata.getProperty());
		}
		//add properties to tableMetadata
		tableMetadata.getColumns().add(columnMetadata);
		if (columnMetadata.isIdentity()) {
			tableMetadata.getKeyColumns().add(columnMetadata);
			tableMetadata.setKeyColumn(columnMetadata.getColumn());
			tableMetadata.setKeyProperty(columnMetadata.getProperty());
		}
		if (columnMetadata.isInsertable()) {
			tableMetadata.getInsertableColumns().add(columnMetadata);
		}
		if (columnMetadata.isUpdatable()) {
			tableMetadata.getUpdatableColumns().add(columnMetadata);
		}
	}

	private static void checkPrimaryKeyTypeValid(Class<?> type, Class<?> entityClass) {
		if (!PrimaryKeyChecker.checkPrimaryKeyTypeValid(type)) {
			throw new UnsupportedKeyTypeException(String.format("The javaType of primary key only supports Long,String and Integer " +
					"but found [%s],entity class:[%s]", type.getName(), entityClass.getName()));
		}
	}

	private static void processPrimaryKeyGenerator(Id id, FieldMetadata fieldMetadata, ColumnMetadata columnMetadata) {
		if (!NonePrimaryKeyGenerator.class.equals(id.keyGenerator())) {
			if (!checkPrimaryKeyGeneratorValid(id, fieldMetadata)) {
				columnMetadata.setKeyGenerator(id.keyGenerator());
				PrimaryKeyGeneratorRepository.registerKeyGenerator(columnMetadata.getTableMetadata().getEntityClass(), id.keyGenerator());
			} else {
				throw new UnsupportedKeyGeneratorException(String.format("The javaType of primary key only supports Long,String and Integer," +
						"the implementation of PrimaryKeyGenerator must be match to the javaType of primary key!" +
						"Entity:[%s],property:[%s]", columnMetadata.getTableMetadata().getEntityClass().getName(), fieldMetadata.getName()));
			}
		}
	}

	private static boolean checkPrimaryKeyGeneratorValid(Id id, FieldMetadata fieldMetadata) {
		return PrimaryKeyChecker.checkPrimaryKeyGeneratorValid(fieldMetadata.getJavaType(), id.keyGenerator());
	}
}
