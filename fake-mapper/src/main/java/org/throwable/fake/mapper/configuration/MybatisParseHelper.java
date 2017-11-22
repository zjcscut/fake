package org.throwable.fake.mapper.configuration;

import org.apache.ibatis.mapping.MappedStatement;
import org.throwable.fake.mapper.exception.EntityTableMappingNotFoundException;

import java.util.Optional;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 15:46
 */
public abstract class MybatisParseHelper {

    public static String getMappedStatementNameSpace(MappedStatement mappedStatement) {
        return mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf("."));
    }

    public static Class<?> getExistEntityClassByMappedStatement(MappedStatement mappedStatement){
       return Optional.of(StaticMapperRegistry.getEntityClassByMappedStatementId(mappedStatement.getId()))
                .orElseThrow(() -> new EntityTableMappingNotFoundException(String.format("Fetch entity mapping for mappedStatementId [%s] failed!", mappedStatement.getId())));
    }
}
