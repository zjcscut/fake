package org.throwable.fake.amqp.support;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/27 17:10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DlxDeclarationMetadata extends DeclarationMetadata {

	protected String dlxQueue;
	protected String dlxExchange;
	protected String dlxRoutingKey;
	protected Integer ttl;

	public DlxDeclarationMetadata(Builder builder) {
		super(builder);
		this.dlxQueue = builder.getDlxQueue();
		this.dlxExchange = builder.getDlxExchange();
		this.dlxRoutingKey = builder.getDlxRoutingKey();
		this.ttl = builder.getTtl();
	}

	public static class Builder extends DeclarationMetadata.Builder {

		protected String dlxQueue;
		protected String dlxExchange;
		protected String dlxRoutingKey;
		protected Integer ttl;

		public String getDlxQueue() {
			return dlxQueue;
		}

		public Builder setDlxQueue(String dlxQueue) {
			this.dlxQueue = dlxQueue;
			return this;
		}

		public String getDlxExchange() {
			return dlxExchange;
		}

		public Builder setDlxExchange(String dlxExchange) {
			this.dlxExchange = dlxExchange;
			return this;
		}

		public String getDlxRoutingKey() {
			return dlxRoutingKey;
		}

		public Builder setDlxRoutingKey(String dlxRoutingKey) {
			this.dlxRoutingKey = dlxRoutingKey;
			return this;
		}

		public Integer getTtl() {
			return ttl;
		}

		public Builder setTtl(Integer ttl) {
			this.ttl = ttl;
			return this;
		}

		public DlxDeclarationMetadata build() {
			return new DlxDeclarationMetadata(this);
		}
	}
}
