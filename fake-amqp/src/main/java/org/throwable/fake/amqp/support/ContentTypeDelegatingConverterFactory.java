package org.throwable.fake.amqp.support;

import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 18:57
 */
public class ContentTypeDelegatingConverterFactory implements AmqpMessageConverterFactory {

	@Override
	public MessageConverter createMessageConverter() {
		return new ContentTypeDelegatingMessageConverter();
	}
}
