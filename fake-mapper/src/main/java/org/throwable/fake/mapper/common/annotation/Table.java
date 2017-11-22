package org.throwable.fake.mapper.common.annotation;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/15 0:25
 */
@Target({ElementType.TYPE})
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

	String value();

	String schema() default "";

	String catalog() default "";
}
