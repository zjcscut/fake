package org.throwable.fake.amqp.support;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/19 10:25
 */
public interface FakeAmqpListenerConfigurer {

	void configureRabbitListeners(FakeAmqpListenerEndpointRegistrar registrar);
}
