package org.throwable.fake.configuration;

import org.springframework.util.ClassUtils;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/16 23:31
 */
public enum FakeModuleEntranceEnum {

	AMQP("org.throwable.fake.amqp.FakeAmqpAutoConfiguration"),

	DURID("org.throwable.fake.amqp.FakeDruidAutoConfiguration"),

	MAPPER("org.throwable.fake.amqp.FakeMapperAutoConfiguration"),

	SWAGGER("org.throwable.fake.amqp.FakeSwaggerAutoConfiguration");

	private final String className;

	FakeModuleEntranceEnum(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public Class<?> getClassFromName() {
		Class<?> classToUse = null;
		try {
			classToUse = ClassUtils.forName(className, FakeModuleEntranceEnum.class.getClassLoader());
		} catch (ClassNotFoundException e) {
			//ignore
		}
		return classToUse;
	}
}
