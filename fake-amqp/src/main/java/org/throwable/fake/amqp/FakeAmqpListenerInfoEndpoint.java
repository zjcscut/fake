package org.throwable.fake.amqp;

import com.google.common.collect.Maps;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.throwable.fake.amqp.common.AmqpConstant;
import org.throwable.fake.amqp.support.FakeAmqpListenerEndpointRegistry;
import org.throwable.fake.core.utils.LocalDateTimeUtils;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/28 23:02
 */
public class FakeAmqpListenerInfoEndpoint extends AbstractEndpoint<Map<String, Object>> implements BeanFactoryAware {

	private DefaultListableBeanFactory beanFactory;

	public FakeAmqpListenerInfoEndpoint() {
		super("fakeAmqpListeners", Boolean.TRUE, Boolean.TRUE);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
	}

	@Override
	public Map<String, Object> invoke() {
		FakeAmqpListenerEndpointRegistry registry
				= this.beanFactory.getBean(AmqpConstant.AMQP_ENDPOINT_REGISTRY_BEAN_NAME, FakeAmqpListenerEndpointRegistry.class);
		Map<String, Object> result = Maps.newHashMap();
		result.put("currentTime", LocalDateTimeUtils.format(LocalDateTime.now()));
		result.put("listeners", buildListenerContainerInfo(registry));
		return result;
	}

	private Map<String, Object> buildListenerContainerInfo(FakeAmqpListenerEndpointRegistry registry) {
		Map<String, Object> listenerContainerInfos = Maps.newHashMap();
		for (Map.Entry<String, MessageListenerContainer> entry : registry.getListenerContainerMap().entrySet()) {
			MessageListenerContainer container = entry.getValue();
			Map<String, Object> pair = Maps.newHashMap();
			if (container instanceof SimpleMessageListenerContainer) {
				SimpleMessageListenerContainer targetContainer = (SimpleMessageListenerContainer) container;
				pair.put("listenerId", targetContainer.getListenerId());
				pair.put("acknowledgeMode", targetContainer.getAcknowledgeMode());
				pair.put("queueNames", targetContainer.getQueueNames());
				pair.put("host", targetContainer.getConnectionFactory().getHost());
				pair.put("port", targetContainer.getConnectionFactory().getPort());
				pair.put("activeConsumerCount", targetContainer.getActiveConsumerCount());
			}
			listenerContainerInfos.put(entry.getKey(), pair);
		}
		return listenerContainerInfos;
	}
}
