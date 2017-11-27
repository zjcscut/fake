package org.throwable.fake.amqp.support;

import lombok.Data;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/27 17:10
 */
@Data
public class DeclarationMetadata {

	protected String queueName;
	protected String exchange;
	protected String exchangeType;
	protected String routingKey;
	protected Map<String, Object> arguments;

	public DeclarationMetadata(Builder builder) {
       this.queueName = builder.getQueueName();
       this.exchange = builder.getExchange();
       this.exchangeType = builder.getExchangeType();
       this.routingKey = builder.getRoutingKey();
       this.arguments = builder.getArguments();
	}

	public static class Builder {

		protected String queueName;
		protected String exchange;
		protected String exchangeType;
		protected String routingKey;
		protected Map<String, Object> arguments;

		public String getQueueName() {
			return queueName;
		}

		public Builder setQueueName(String queueName) {
			this.queueName = queueName;
			return this;
		}

		public String getExchange() {
			return exchange;
		}

		public Builder setExchange(String exchange) {
			this.exchange = exchange;
			return this;
		}

		public String getExchangeType() {
			return exchangeType;
		}

		public Builder setExchangeType(String exchangeType) {
			this.exchangeType = exchangeType;
			return this;
		}

		public String getRoutingKey() {
			return routingKey;
		}

		public Builder setRoutingKey(String routingKey) {
			this.routingKey = routingKey;
			return this;
		}

		public Map<String, Object> getArguments() {
			return arguments;
		}

		public Builder setArguments(Map<String, Object> arguments) {
			this.arguments = arguments;
			return this;
		}

		public DeclarationMetadata build() {
			return new DeclarationMetadata(this);
		}
	}
}
