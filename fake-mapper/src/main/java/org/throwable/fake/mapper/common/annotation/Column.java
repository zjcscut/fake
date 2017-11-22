package org.throwable.fake.mapper.common.annotation;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.throwable.fake.mapper.support.plugins.generator.type.NoneTypeHandler;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/15 0:30
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

	String value() default "";

	boolean insertable() default true;

	boolean updatable() default true;

	boolean nullable() default false;

	Class<? extends TypeHandler<?>> typeHandler() default NoneTypeHandler.class;

	JdbcType jdbcType() default JdbcType.UNDEFINED;
}
