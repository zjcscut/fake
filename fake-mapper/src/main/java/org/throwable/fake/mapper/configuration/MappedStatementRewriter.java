package org.throwable.fake.mapper.configuration;

import org.apache.ibatis.mapping.MappedStatement;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 15:19
 */
public interface MappedStatementRewriter {

    void rewriteMappedStatement(MappedStatement mappedStatement, MapperMethodMetadata mapperMethodMetadata);
}
