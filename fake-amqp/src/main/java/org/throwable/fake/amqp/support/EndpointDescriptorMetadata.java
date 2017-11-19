package org.throwable.fake.amqp.support;

import org.springframework.amqp.core.AcknowledgeMode;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/19 9:55
 */
public final class EndpointDescriptorMetadata {

	private final String containerFactoryBeanName;
	private final AcknowledgeMode acknowledgeMode;
	private final int concurrentConsumers;
	private final int maxConcurrentConsumers;

	public EndpointDescriptorMetadata(String containerFactoryBeanName,
									  AcknowledgeMode acknowledgeMode,
									  int concurrentConsumers,
									  int maxConcurrentConsumers) {
		this.containerFactoryBeanName = containerFactoryBeanName;
		this.acknowledgeMode = acknowledgeMode;
		this.concurrentConsumers = concurrentConsumers;
		this.maxConcurrentConsumers = maxConcurrentConsumers;
	}

	public String getContainerFactoryBeanName() {
		return containerFactoryBeanName;
	}

	public AcknowledgeMode getAcknowledgeMode() {
		return acknowledgeMode;
	}

	public int getConcurrentConsumers() {
		return concurrentConsumers;
	}

	public int getMaxConcurrentConsumers() {
		return maxConcurrentConsumers;
	}
}
