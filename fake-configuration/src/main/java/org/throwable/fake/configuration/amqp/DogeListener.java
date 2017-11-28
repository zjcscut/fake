package org.throwable.fake.configuration.amqp;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/28 22:33
 */
public class DogeListener implements MessageListener{

	@Override
	public void onMessage(Message message) {
		System.out.println("DogeListener receive message -> " + new String(message.getBody()));
	}
}
