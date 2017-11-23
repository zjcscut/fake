package org.throwable.fake.configuration;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/16 23:38
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
@Import(value = {FakeAutoConfiguration.class})
public @interface EnableFake {

	FakeModuleEntranceEnum[] modules() default {
			FakeModuleEntranceEnum.AMQP,
			FakeModuleEntranceEnum.SWAGGER,
			FakeModuleEntranceEnum.MAPPER,
			FakeModuleEntranceEnum.DURID,
	};
}
