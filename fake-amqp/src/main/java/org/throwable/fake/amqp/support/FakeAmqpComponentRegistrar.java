package org.throwable.fake.amqp.support;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.throwable.fake.amqp.FakeAmqpProperties;
import org.throwable.fake.amqp.common.AmqpConstant;
import org.throwable.fake.amqp.exception.FakeAmqpException;
import org.throwable.fake.spring.support.BeanDefinitionRegisterAssistor;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/28 12:32
 */
public final class FakeAmqpComponentRegistrar implements BeanFactoryAware {

	private final FakeAmqpComponentDeclarer fakeAmqpComponentDeclarer;
	private final FakeAmqpListenerEndpointRegistrar fakeAmqpListenerEndpointRegistrar;
	private final FakeAmqpProperties fakeAmqpProperties;
	private DefaultListableBeanFactory beanFactory;

	public FakeAmqpComponentRegistrar(FakeAmqpComponentDeclarer fakeAmqpComponentDeclarer,
									  FakeAmqpListenerEndpointRegistrar fakeAmqpListenerEndpointRegistrar,
									  FakeAmqpProperties fakeAmqpProperties) {
		this.fakeAmqpComponentDeclarer = fakeAmqpComponentDeclarer;
		this.fakeAmqpListenerEndpointRegistrar = fakeAmqpListenerEndpointRegistrar;
		this.fakeAmqpProperties = fakeAmqpProperties;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
	}

	public void registerAllAmqpComponents() {
		registerAllCommonQueues();
		registerAllDlxQueues();
		registerAllListeners();
	}

	public void registerAllCommonQueues() {
		if (null != fakeAmqpProperties.getQueues() && !fakeAmqpProperties.getQueues().isEmpty()) {
			for (Map.Entry<String, FakeAmqpProperties.QueueConfigurationPair> entry : fakeAmqpProperties.getQueues().entrySet()) {
				FakeAmqpProperties.QueueConfigurationPair queue = entry.getValue();
				DeclarationMetadata metadata = new DeclarationMetadata.Builder()
						.setQueueName(queue.getQueueName())
						.setExchange(queue.getExchangeName())
						.setExchangeType(queue.getExchangeType())
						.setRoutingKey(queue.getRoutingKey())
						.setArguments(queue.getArguments())
						.build();
				fakeAmqpComponentDeclarer.declareBinding(metadata);
			}
		}
	}

	public void registerAllDlxQueues() {
		if (null != fakeAmqpProperties.getDlx() && !fakeAmqpProperties.getDlx().isEmpty()) {
			for (Map.Entry<String, FakeAmqpProperties.DlxQueueConfigurationPair> entry : fakeAmqpProperties.getDlx().entrySet()) {
				FakeAmqpProperties.DlxQueueConfigurationPair dlx = entry.getValue();
				DlxDeclarationMetadata metadata = new DlxDeclarationMetadata.Builder()
						.setQueueName(dlx.getQueueName())
						.setExchange(dlx.getExchangeName())
						.setExchangeType(dlx.getExchangeType())
						.setRoutingKey(dlx.getRoutingKey())
						.setArguments(dlx.getArguments())
						.setDlxQueue(dlx.getDlxQueue())
						.setDlxExchange(dlx.getDlxExchange())
						.setDlxRoutingKey(dlx.getDlxRoutingKey())
						.build();
				fakeAmqpComponentDeclarer.declareDlxBinding(metadata);
			}
		}
	}

	public void registerAllListeners() {
		if (null != fakeAmqpProperties.getListeners() && !fakeAmqpProperties.getListeners().isEmpty()) {
			for (Map.Entry<String, FakeAmqpProperties.ListenerConfigurationPair> entry : fakeAmqpProperties.getListeners().entrySet()) {
				String id = entry.getKey();
				if (!StringUtils.hasText(id)) {
					id = String.format("%s#%s", AmqpConstant.AMQP_LISTENER_ENDPOINT_ID_PREFIX, AmqpConstant.ENDPOINT_COUNTER.getAndIncrement());
				}
				FakeAmqpProperties.ListenerConfigurationPair value = entry.getValue();
				fakeAmqpListenerEndpointRegistrar.registerMessageListenerContainer(checkListenerParametersAndBuildMetadata(id, value));
			}
		}
	}

	private MessageListenerContainerMetadata checkListenerParametersAndBuildMetadata(String id, FakeAmqpProperties.ListenerConfigurationPair pair) {
		if (!StringUtils.hasText(pair.getGroup())) {
			pair.setGroup(AmqpConstant.AMQP_LISTENER_ENDPOINT_GROUP);
		}
		Assert.hasText(pair.getQueueName(), "QueueName must be set!");
		Assert.hasText(pair.getAcknowledgeMode(), "AcknowledgeMode must be set!");
		AcknowledgeMode acknowledgeMode = resolveAcknowledgeMode(pair.getAcknowledgeMode());
		Assert.hasText(pair.getListenerClassName(), "ListenerClassName must be set!");
		Object listenerBean;
		try {
			Class<?> listenerClass = ClassUtils.forName(pair.getListenerClassName(), FakeAmqpComponentRegistrar.class.getClassLoader());
			Assert.isTrue(MessageListener.class.isAssignableFrom(listenerClass)
							|| ChannelAwareMessageListener.class.isAssignableFrom(listenerClass),
					"ListenerClass must be instance of MessageListener or ChannelAwareMessageListener!");
			BeanDefinitionRegisterAssistor.registerBeanDefinitionFromCommonClass(listenerClass, beanFactory);
			listenerBean = beanFactory.getBean(listenerClass);
		} catch (ClassNotFoundException e) {
			throw new FakeAmqpException(e);
		}
		if (StringUtils.hasText(pair.getExchangeName()) && StringUtils.hasText(pair.getExchangeType())
				&& StringUtils.hasText(pair.getRoutingKey())) {
			DeclarationMetadata metadata = new DeclarationMetadata.Builder()
					.setQueueName(pair.getQueueName())
					.setExchange(pair.getExchangeName())
					.setExchangeType(pair.getExchangeType())
					.setRoutingKey(pair.getRoutingKey())
					.setArguments(pair.getArguments())
					.build();
			fakeAmqpComponentDeclarer.declareBinding(metadata);
		}
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setMessageListener(listenerBean);
		container.setQueueNames(pair.getQueueName().split(AmqpConstant.QUEUE_SEPARATOR));
		container.setAcknowledgeMode(acknowledgeMode);
		container.setConcurrentConsumers(pair.getConcurrentConsumers());
		container.setMaxConcurrentConsumers(pair.getMaxConcurrentConsumers());
		container.setRabbitAdmin(FakeAmqpComponentUtils.getExistingRabbitAdmin(beanFactory));
		container.setConnectionFactory(FakeAmqpComponentUtils.getExistingCachingConnectionFactory(beanFactory));
		container.setMessageConverter(FakeAmqpComponentUtils.getContentTypeDelegatingMessageConverter());
		container.setAutoStartup(Boolean.TRUE);
		container.setListenerId(id);
		return new MessageListenerContainerMetadata.Builder()
				.setId(id)
				.setGroup(pair.getGroup())
				.setMessageListenerContainer(container)
				.build();
	}

	private AcknowledgeMode resolveAcknowledgeMode(String acknowledgeMode) {
		String upperMode = acknowledgeMode.toUpperCase();
		if (upperMode.equals(AcknowledgeMode.NONE.name())) {
			return AcknowledgeMode.NONE;
		} else if (upperMode.equals(AcknowledgeMode.AUTO.name())) {
			return AcknowledgeMode.AUTO;
		} else if (upperMode.equals(AcknowledgeMode.MANUAL.name())) {
			return AcknowledgeMode.MANUAL;
		}
		throw new IllegalArgumentException(String.format("Invalid acknowledgeMode value [%s]!", acknowledgeMode));
	}


}
