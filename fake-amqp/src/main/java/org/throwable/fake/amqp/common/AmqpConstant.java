package org.throwable.fake.amqp.common;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 18:52
 */
public class AmqpConstant {

	public static final String AMQP_BEAN_POST_PROCESSOR_BEAN_NAME = "fakeAmqpAnnotationListenerAnnotationBeanPostProcessor";

	public static final String AMQP_ENDPOINT_REGISTRAR_BEAN_NAME = "fakeAmqpListenerEndpointRegistrar";

	public static final String AMQP_ENDPOINT_REGISTRY_BEAN_NAME = "fakeAmqpListenerEndpointRegistry";

	public static final String AMQP_CONTAINER_FACTORY_BEAN_NAME = "fakeAmqpListenerContainerFactory";

	public static final String AMQP_DECLARER_BEAN_NAME = "fakeAmqpComponentDeclarer";

	public static final String AMQP_HANDLER_METHOD_FACTORY_ADAPTER_BEAN_NAME = "fakeAmqpHandlerMethodFactoryAdapter";

	public static final String BEAN_EXPRESSION_RESOLVER_DELEGATOR_BEAN_NAME = "beanExpressionResolverDelegator";

	public static final String AMQP_COMPONENT_REGISTRAR_BEAN_NAME = "fakeAmqpComponentRegistrar";

	public static final String AMQP_LISTENER_ENDPOINT_ID_PREFIX = "org.springframework.amqp.rabbit.RabbitListenerEndpointContainer";
}
