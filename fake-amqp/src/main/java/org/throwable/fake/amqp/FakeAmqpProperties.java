package org.throwable.fake.amqp;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 18:42
 */
@ConfigurationProperties(prefix = FakeAmqpProperties.PREFIX)
public class FakeAmqpProperties {

	public static final String PREFIX = "fake.amqp";
}
