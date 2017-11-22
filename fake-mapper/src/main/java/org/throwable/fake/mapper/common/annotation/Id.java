package org.throwable.fake.mapper.common.annotation;

import org.throwable.fake.mapper.support.plugins.generator.identity.NonePrimaryKeyGenerator;
import org.throwable.fake.mapper.support.plugins.generator.identity.PrimaryKeyGenerator;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/15 0:07
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {

	String value() default "";

	boolean autoIncrement() default true;

	Class<? extends PrimaryKeyGenerator> keyGenerator() default NonePrimaryKeyGenerator.class;
}
