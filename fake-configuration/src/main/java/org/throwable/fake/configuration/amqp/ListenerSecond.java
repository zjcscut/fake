package org.throwable.fake.configuration.amqp;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.stereotype.Component;
import org.throwable.fake.amqp.annotation.FakeAmqpListener;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/19 10:36
 */
@Component
public class ListenerSecond {

	@FakeAmqpListener(rabbitAdmin = "amqpAdmin",
			bindings = @QueueBinding(
					value = @Queue(value = "doge-second", durable = "true"),
					exchange = @Exchange(value = "doge-second"),
					key = "doge-second"),
			concurrentConsumers = 5,
			maxConcurrentConsumers = 10
	)
	public void entrance(Message message) {
		System.out.println(String.format("%s-message --> %s", "doge-second", new String(message.getBody())));
	}
}
