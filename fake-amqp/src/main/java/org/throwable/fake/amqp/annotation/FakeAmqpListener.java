package org.throwable.fake.amqp.annotation;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 19:05
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Documented
@MessageMapping
@Repeatable(value = FakeAmqpListeners.class)
public @interface FakeAmqpListener {

	String id() default "";

	String group() default "";

	boolean exclusive() default false;

	String rabbitAdmin() default "";

	AcknowledgeMode acknowledgeMode() default AcknowledgeMode.NONE;

	boolean autoDeclare() default true;

	String[] queues() default {};

	int concurrentConsumers() default 1;

	int maxConcurrentConsumers() default 5;

	String priority() default "";

	QueueBinding[] bindings() default {};
}
