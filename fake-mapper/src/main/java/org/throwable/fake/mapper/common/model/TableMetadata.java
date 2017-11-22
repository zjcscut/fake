package org.throwable.fake.mapper.common.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.throwable.fake.mapper.common.annotation.Table;
import org.throwable.fake.mapper.utils.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/15 0:10
 */
@Getter
@Setter
public class TableMetadata implements Serializable{

    private static final long serialVersionUID = -1;

    //table name
    private String name;
    private String schema;
    private String catalog;
    private Class<?> entityClass;

    //all ColumnMetadata
    private Set<ColumnMetadata> columns;
    //key ColumnMetadata
    private Set<ColumnMetadata> keyColumns;
    //none key ColumnMetadata
    private Set<ColumnMetadata> noneKeyColumns;

    //insertable ColumnMetadata
    private Set<ColumnMetadata> insertableColumns;
    //updatable ColumnMetadata
    private Set<ColumnMetadata> updatableColumns;

    //key property names
    private List<String> keyPropertyNames;
    //key column names
    private List<String> keyColumnNames;

    //property name -> ColumnMetadata
    private Map<String, ColumnMetadata> propertyMap;

    //resultMap
    private ResultMap resultMap;

    public TableMetadata(Class<?> entityClass) {
        this.entityClass = entityClass;
    }

    public void setTableAnnotation(Table table) {
        this.name = table.value();
        this.schema = table.schema();
        this.catalog = table.catalog();
    }

    public String getTableNameWithPrefix() {
        if (StringUtils.isNotBlank(catalog)) {
            return catalog + "." + name;
        }
        if (StringUtils.isNotBlank(schema)) {
            return schema + "." + name;
        }
        return name;
    }

    public void setKeyProperty(String keyPropertyName) {
        if (null == this.keyPropertyNames) {
            this.keyPropertyNames = new ArrayList<>();
            this.keyPropertyNames.add(keyPropertyName);
        } else {
            this.keyPropertyNames.add(keyPropertyName);
        }
    }

    public void setKeyColumn(String keyColumnName) {
        if (null == this.keyColumnNames) {
            this.keyColumnNames = new ArrayList<>();
            this.keyColumnNames.add(keyColumnName);
        } else {
            this.keyColumnNames.add(keyColumnName);
        }
    }

    public ResultMap createBaseResultMap(final Configuration configuration) {
        if (null != this.resultMap) {
            return this.resultMap;
        }
        if (null == columns || columns.isEmpty()) {
            return null;
        }
        List<ResultMapping> resultMappings = columns.stream().map(column -> {
            ResultMapping.Builder builder = new ResultMapping.Builder(
                    configuration,
                    column.getProperty());
            builder.column(column.getColumn());
            if (null != column.getJdbcType()) {
                builder.jdbcType(column.getJdbcType());
            }
            if (null != column.getJavaType()) {
                builder.javaType(column.getJavaType());
            }
            List<ResultFlag> flags = new ArrayList<>();
            if (column.isIdentity()) {
                flags.add(ResultFlag.ID);
            }
            builder.flags(flags);
            return builder.build();
        }).collect(Collectors.toList());
        ResultMap.Builder builder = new ResultMap.Builder(configuration, "baseResultMap", this.entityClass, resultMappings, true);
        this.resultMap = builder.build();
        return this.resultMap;
    }

    public void initPropertyMap() {
        if (null == this.propertyMap) {
            this.propertyMap = new HashMap<>(columns.size());
        }
        for (ColumnMetadata columnMetadata : columns) {
            this.propertyMap.put(columnMetadata.getProperty(), columnMetadata);
        }
    }
}
