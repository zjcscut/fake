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

	public EndpointDescriptorMetadata(Builder builder) {
		this.containerFactoryBeanName = builder.getContainerFactoryBeanName();
		this.acknowledgeMode = builder.getAcknowledgeMode();
		this.concurrentConsumers = builder.getConcurrentConsumers();
		this.maxConcurrentConsumers = builder.getMaxConcurrentConsumers();
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


	public static class Builder{

		private String containerFactoryBeanName;
		private AcknowledgeMode acknowledgeMode;
		private int concurrentConsumers;
		private int maxConcurrentConsumers;

		public String getContainerFactoryBeanName() {
			return containerFactoryBeanName;
		}

		public Builder setContainerFactoryBeanName(String containerFactoryBeanName) {
			this.containerFactoryBeanName = containerFactoryBeanName;
			return this;
		}

		public AcknowledgeMode getAcknowledgeMode() {
			return acknowledgeMode;
		}

		public Builder setAcknowledgeMode(AcknowledgeMode acknowledgeMode) {
			this.acknowledgeMode = acknowledgeMode;
			return this;
		}

		public int getConcurrentConsumers() {
			return concurrentConsumers;
		}

		public Builder setConcurrentConsumers(int concurrentConsumers) {
			this.concurrentConsumers = concurrentConsumers;
			return this;
		}

		public int getMaxConcurrentConsumers() {
			return maxConcurrentConsumers;
		}

		public Builder setMaxConcurrentConsumers(int maxConcurrentConsumers) {
			this.maxConcurrentConsumers = maxConcurrentConsumers;
			return this;
		}

		public EndpointDescriptorMetadata build(){
			return new EndpointDescriptorMetadata(this);
		}
	}

}
