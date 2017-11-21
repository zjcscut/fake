package org.throwable.fake.amqp.support;

import org.springframework.amqp.support.converter.MessageConverter;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 18:53
 */
public interface AmqpMessageConverterFactory {

	/**
	 * create MessageConverter
	 * @return MessageConverter
	 */
	MessageConverter createMessageConverter();
}
