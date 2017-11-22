package org.throwable.fake.mapper.support.assistant;

import com.google.common.collect.Sets;
import org.throwable.fake.mapper.common.model.ColumnMetadata;
import org.throwable.fake.mapper.support.plugins.condition.filter.FieldFilter;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/18 0:38
 */
public abstract class FieldFilterAssistant extends SqlAppendMetadataAssistant {

	public static Set<ColumnMetadata> filter(Set<ColumnMetadata> entityColumns, FieldFilter fieldFilter) {
		if (null == entityColumns || entityColumns.size() == 0) {
			return Sets.newHashSet();
		}
		Set<String> filterFields = fieldFilter.accept();
		if (fieldFilter.isIncludeFilter()) {
			return entityColumns.stream()
					.filter(each -> filterFields.contains(each.getProperty()))
					.collect(Collectors.toSet());
		} else {
			return entityColumns.stream()
					.filter(each -> !filterFields.contains(each.getProperty()))
					.collect(Collectors.toSet());
		}
	}

	public static Set<ColumnMetadata> getFilterColumns(Class<?> entityClass, FieldFilter fieldFilter, boolean skipPrimaryKey) {
		Set<ColumnMetadata> columnList = skipPrimaryKey ? getNonePrimaryColumnMetadata(entityClass) : getAllColumnMetadata(entityClass);
		if (null != fieldFilter) {
			columnList = filter(columnList, fieldFilter);
		}
		return columnList;
	}
}
