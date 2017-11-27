package org.throwable.fake.amqp.support;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/27 22:52
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ListenerDeclarationMetadata extends DeclarationMetadata {

	protected String acknowledgeMode;
	protected Integer concurrentConsumers;
	protected Integer maxConcurrentConsumers;
	protected String listenerClassName;

	public ListenerDeclarationMetadata(Builder builder) {
		super(builder);
		this.acknowledgeMode = builder.getAcknowledgeMode();
		this.concurrentConsumers = builder.getConcurrentConsumers();
		this.maxConcurrentConsumers = builder.getMaxConcurrentConsumers();
		this.listenerClassName = builder.getListenerClassName();
	}

	public static class Builder extends DeclarationMetadata.Builder {

		protected String acknowledgeMode;
		protected Integer concurrentConsumers;
		protected Integer maxConcurrentConsumers;
		protected String listenerClassName;

		public String getAcknowledgeMode() {
			return acknowledgeMode;
		}

		public Builder setAcknowledgeMode(String acknowledgeMode) {
			this.acknowledgeMode = acknowledgeMode;
			return this;
		}

		public Integer getConcurrentConsumers() {
			return concurrentConsumers;
		}

		public Builder setConcurrentConsumers(Integer concurrentConsumers) {
			this.concurrentConsumers = concurrentConsumers;
			return this;
		}

		public Integer getMaxConcurrentConsumers() {
			return maxConcurrentConsumers;
		}

		public Builder setMaxConcurrentConsumers(Integer maxConcurrentConsumers) {
			this.maxConcurrentConsumers = maxConcurrentConsumers;
			return this;
		}

		public String getListenerClassName() {
			return listenerClassName;
		}

		public Builder setListenerClassName(String listenerClassName) {
			this.listenerClassName = listenerClassName;
			return this;
		}

		public ListenerDeclarationMetadata build() {
			return new ListenerDeclarationMetadata(this);
		}
	}
}
