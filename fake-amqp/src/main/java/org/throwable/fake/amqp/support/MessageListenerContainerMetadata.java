package org.throwable.fake.amqp.support;

import lombok.Data;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/28 15:59
 */
@Data
public class MessageListenerContainerMetadata {

	private final MessageListenerContainer messageListenerContainer;
	private final String id;
	private final String group;

	public MessageListenerContainerMetadata(Builder builder) {
		this.messageListenerContainer = builder.getMessageListenerContainer();
		this.id = builder.getId();
		this.group = builder.getGroup();
	}

	public static class Builder {

		private MessageListenerContainer messageListenerContainer;
		private String id;
		private String group;

		public MessageListenerContainer getMessageListenerContainer() {
			return messageListenerContainer;
		}

		public Builder setMessageListenerContainer(MessageListenerContainer messageListenerContainer) {
			this.messageListenerContainer = messageListenerContainer;
			return this;
		}

		public String getId() {
			return id;
		}

		public Builder setId(String id) {
			this.id = id;
			return this;
		}

		public String getGroup() {
			return group;
		}

		public Builder setGroup(String group) {
			this.group = group;
			return this;
		}

		public MessageListenerContainerMetadata build() {
			return new MessageListenerContainerMetadata(this);
		}
	}
}
