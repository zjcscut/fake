package org.throwable.fake.mapper.configuration;

import org.throwable.fake.mapper.common.annotation.Table;
import org.throwable.fake.mapper.common.model.FieldMetadata;
import org.throwable.fake.mapper.common.model.TableMetadata;
import org.throwable.fake.mapper.exception.MetadataParseException;
import org.throwable.fake.mapper.utils.CollectionUtils;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 0:49
 */
public abstract class TableMetadataParser {

    public static TableMetadata processTableMetadata(Class<?> entityClass) {
        if (StaticMapperRegistry.containsTableMetadata(entityClass)) {
            return null;
        }
        if (!entityClass.isAnnotationPresent(Table.class)) {
            return null;
        }
        Table table = entityClass.getAnnotation(Table.class);
        TableMetadata tableMetadata = new TableMetadata(entityClass);
        tableMetadata.setTableAnnotation(table);
        tableMetadata.setColumns(new LinkedHashSet<>());
        tableMetadata.setKeyColumns(new LinkedHashSet<>(1));
        tableMetadata.setInsertableColumns(new LinkedHashSet<>());
        tableMetadata.setUpdatableColumns(new LinkedHashSet<>());
        List<FieldMetadata> fieldMetadataList = FieldMetadataParser.getMergeFieldMetadata(entityClass);
        if (null != fieldMetadataList && !fieldMetadataList.isEmpty()) {
            for (FieldMetadata fieldMetadata : fieldMetadataList) {
                ColumnMetadataParser.processColumnMetadata(tableMetadata, fieldMetadata);
            }
        }
        checkTableMetadataValid(tableMetadata);
        tableMetadata.setNoneKeyColumns(CollectionUtils.deepCopySet(tableMetadata.getColumns()));
        tableMetadata.getNoneKeyColumns().removeAll(tableMetadata.getKeyColumns());
        tableMetadata.initPropertyMap();
        return tableMetadata;
    }

    private static void checkTableMetadataValid(TableMetadata tableMetadata) {
        if (tableMetadata.getKeyColumns().size() != 1) {
            throw new MetadataParseException(String.format("Mapper only supports one primary key," +
                    "this problem will be resolved in the future version,entity class:[%s]", tableMetadata.getEntityClass().getName()));
        }
    }
}
