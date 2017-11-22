package org.throwable.fake.mapper.support.assistant;

import org.throwable.fake.mapper.common.model.ColumnMetadata;
import org.throwable.fake.mapper.configuration.StaticMapperRegistry;
import org.throwable.fake.mapper.exception.ColumnNotFoundException;
import org.throwable.fake.mapper.exception.EntityTableMappingNotFoundException;
import org.throwable.fake.mapper.exception.PrimaryKeyNotFoundException;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.throwable.fake.mapper.common.constant.Constants.DOT;
import static org.throwable.fake.mapper.common.constant.Constants.PARAM_DEFAULT;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 20:12
 */
public abstract class SqlAppendMetadataAssistant {

	public static String getParameterPrefix(String parameterName) {
		return Objects.equals(parameterName, PARAM_DEFAULT) ? "" : parameterName;
	}

	public static String getParameterPrefixWithDot(String parameterName) {
		return Objects.equals(parameterName, PARAM_DEFAULT) ? "" : parameterName.concat(DOT);
	}

	public static String getTestClause(String content) {
		return String.format("<if test=\"%s\"/>\n", content);
	}

	public static String getIfNotNull(String testValue, String content) {
		return String.format("<if test=\"%s neq null\">%s</if>", testValue, content);
	}

	public static String getIfNotEmpty(String testValue, String content) {
		return String.format("<if test=\"%s neq null and %s neq ''\">%s</if>", testValue, testValue, content);
	}

	public static String fetchExistTableName(Class<?> entityClass) {
		return Optional.of(StaticMapperRegistry.getTableMetadataByEntityClass(entityClass))
				.orElseThrow(() -> new EntityTableMappingNotFoundException(String.format("Entity table mapping for class [%s] could not be found!", entityClass.getName())))
				.getTableNameWithPrefix();
	}

	public static ColumnMetadata fetchPrimaryColumnMetadata(Class<?> entityClass) {
		if (StaticMapperRegistry.containsTableMetadata(entityClass)) {
			return StaticMapperRegistry.getTableMetadataByEntityClass(entityClass)
					.getKeyColumns().iterator().next();
		}
		return null;
	}

	public static ColumnMetadata fetchExistPrimaryColumnMetadata(Class<?> entityClass) {
		return Optional.of(StaticMapperRegistry.getTableMetadataByEntityClass(entityClass)
				.getKeyColumns().iterator().next())
				.orElseThrow(() -> new PrimaryKeyNotFoundException(String.format("Could not find primary key metadata for class [%s]", entityClass.getName())));
	}

	public static Set<ColumnMetadata> getAllColumnMetadata(Class<?> entityClass) {
		if (StaticMapperRegistry.containsTableMetadata(entityClass)) {
			return StaticMapperRegistry.getTableMetadataByEntityClass(entityClass).getColumns();
		}
		return null;
	}

	public static Set<ColumnMetadata> getAllExistColumnMetadata(Class<?> entityClass) {
		return Optional.of(StaticMapperRegistry.getTableMetadataByEntityClass(entityClass)
				.getColumns()).orElseThrow(() -> new ColumnNotFoundException(String.format("Could not find column metadata for class [%s]", entityClass.getName())));
	}


	public static Set<ColumnMetadata> getNonePrimaryColumnMetadata(Class<?> entityClass) {
		if (StaticMapperRegistry.containsTableMetadata(entityClass)) {
			return StaticMapperRegistry.getTableMetadataByEntityClass(entityClass).getNoneKeyColumns();
		}
		return null;
	}

	public static Set<ColumnMetadata> getExistNonePrimaryColumnMetadata(Class<?> entityClass) {
		return Optional.of(StaticMapperRegistry.getTableMetadataByEntityClass(entityClass)
				.getNoneKeyColumns()).orElseThrow(() -> new ColumnNotFoundException(String.format("Could not find none key column metadata for class [%s]", entityClass.getName())));
	}

	public static Set<ColumnMetadata> getExistInsertableColumnMetadata(Class<?> entityClass, boolean skipPrimaryKey) {
		return skipPrimaryKey ? getExistNonePrimaryColumnMetadata(entityClass)
				.stream()
				.filter(ColumnMetadata::isInsertable)
				.collect(Collectors.toSet())
				: getAllExistColumnMetadata(entityClass)
				.stream()
				.filter(ColumnMetadata::isInsertable)
				.collect(Collectors.toSet());
	}

	public static Set<ColumnMetadata> getExistUpdatableColumnMetadata(Class<?> entityClass, boolean skipPrimaryKey) {
		return skipPrimaryKey ? getExistNonePrimaryColumnMetadata(entityClass)
				.stream()
				.filter(ColumnMetadata::isUpdatable)
				.collect(Collectors.toSet())
				: getAllExistColumnMetadata(entityClass)
				.stream()
				.filter(ColumnMetadata::isUpdatable)
				.collect(Collectors.toSet());
	}
}
