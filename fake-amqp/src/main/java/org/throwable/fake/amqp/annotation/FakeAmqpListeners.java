package org.throwable.fake.amqp.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 19:05
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
@Documented
public @interface FakeAmqpListeners {

	FakeAmqpListener[] value();
}
