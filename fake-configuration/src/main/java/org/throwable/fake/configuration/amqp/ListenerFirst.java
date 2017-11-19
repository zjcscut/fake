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
public class ListenerFirst {

	@FakeAmqpListener(rabbitAdmin = "amqpAdmin",
			bindings = @QueueBinding(
					value = @Queue(value = "doge-first",durable = "true"),
					exchange = @Exchange(value = "doge-first"),
					key = "doge-first")
			)
	public void entrance(Message message) {
		System.out.println(String.format("%s-message --> %s","doge-first",new String(message.getBody())));
	}
}
