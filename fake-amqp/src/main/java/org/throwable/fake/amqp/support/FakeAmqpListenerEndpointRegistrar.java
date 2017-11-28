package org.throwable.fake.amqp.support;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpoint;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/19 9:26
 */
public class FakeAmqpListenerEndpointRegistrar implements BeanFactoryAware, InitializingBean {

	private final Set<String> registeredEndpointIds = new HashSet<>();
	private final List<AmqpListenerEndpointDescriptor> endpointDescriptors = new ArrayList<>();
	private MessageHandlerMethodFactory messageHandlerMethodFactory;
	private DefaultListableBeanFactory beanFactory;
	private FakeAmqpListenerEndpointRegistry endpointRegistry;
	private boolean startImmediately;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
	}

	@Override
	public void afterPropertiesSet() {

	}

	public FakeAmqpListenerEndpointRegistry getEndpointRegistry() {
		return endpointRegistry;
	}

	public void setEndpointRegistry(FakeAmqpListenerEndpointRegistry endpointRegistry) {
		this.endpointRegistry = endpointRegistry;
	}

	public MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
		return messageHandlerMethodFactory;
	}

	public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory) {
		this.messageHandlerMethodFactory = messageHandlerMethodFactory;
	}

	public void registerAllEndpoints() {
		synchronized (this.endpointDescriptors) {
			for (AmqpListenerEndpointDescriptor descriptor : this.endpointDescriptors) {
				RabbitListenerEndpoint endpoint = descriptor.getEndpoint();
				if (!this.registeredEndpointIds.contains(endpoint.getId())) {
					this.endpointRegistry.registerListenerContainer(endpoint, resolveContainerFactory(descriptor));
					this.registeredEndpointIds.add(endpoint.getId());
				}
			}
			this.startImmediately = true;
		}
	}

	public void registerEndpoint(RabbitListenerEndpoint endpoint, EndpointDescriptorMetadata metadata) {
		Assert.notNull(endpoint, "Endpoint must be set");
		Assert.hasText(endpoint.getId(), "Endpoint id must be set");
		Assert.hasText(endpoint.getGroup(), "Endpoint group must be set");
		if (!registeredEndpointIds.contains(endpoint.getId())) {
			AmqpListenerEndpointDescriptor descriptor = new AmqpListenerEndpointDescriptor(endpoint, metadata);
			synchronized (this.endpointDescriptors) {
				if (this.startImmediately) {
					Assert.notNull(this.endpointRegistry, "EndpointRegistry must be set");
					this.endpointRegistry.registerListenerContainer(endpoint, resolveContainerFactory(descriptor), Boolean.TRUE);
					registeredEndpointIds.add(endpoint.getId());
				} else {
					this.endpointDescriptors.add(descriptor);
				}
			}
		}
	}

	private RabbitListenerContainerFactory<?> resolveContainerFactory(AmqpListenerEndpointDescriptor descriptor) {
		EndpointDescriptorMetadata metadata = descriptor.getMetadata();
		Assert.state(this.beanFactory != null, "BeanFactory must be set to obtain container factory by bean name");
		Assert.state(metadata != null, "EndpointDescriptorMetadata must be set to process container factory");
		SimpleRabbitListenerContainerFactory containerFactory =
				this.beanFactory.getBean(metadata.getContainerFactoryBeanName(), SimpleRabbitListenerContainerFactory.class);
		containerFactory.setAcknowledgeMode(metadata.getAcknowledgeMode());
		containerFactory.setMaxConcurrentConsumers(metadata.getMaxConcurrentConsumers());
		containerFactory.setConcurrentConsumers(metadata.getConcurrentConsumers());
		return containerFactory;
	}

	private static final class AmqpListenerEndpointDescriptor {

		private final RabbitListenerEndpoint endpoint;
		private final EndpointDescriptorMetadata metadata;

		public AmqpListenerEndpointDescriptor(RabbitListenerEndpoint endpoint,
											  EndpointDescriptorMetadata metadata) {
			this.endpoint = endpoint;
			this.metadata = metadata;
		}

		public RabbitListenerEndpoint getEndpoint() {
			return endpoint;
		}

		public EndpointDescriptorMetadata getMetadata() {
			return metadata;
		}
	}
}
