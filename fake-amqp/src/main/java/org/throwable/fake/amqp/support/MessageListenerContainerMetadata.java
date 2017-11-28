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

    private MessageListenerContainer messageListenerContainer;

}
