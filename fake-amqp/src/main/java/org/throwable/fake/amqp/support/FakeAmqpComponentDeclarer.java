package org.throwable.fake.amqp.support;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/27 15:27
 */
@Slf4j
public final class FakeAmqpComponentDeclarer implements BeanFactoryAware {

	private static final Boolean DEFAULT_DURABLE = Boolean.TRUE;
	private static final Boolean DEFAULT_AUTO_DELETE = Boolean.FALSE;
	private static final Boolean DEFAULT_EXCLUSIVE = Boolean.FALSE;
	private static final String EMPTY = "";
	private static final String DLX_EXCHANGE_KEY = "x-dead-letter-exchange";
	private static final String DLX_ROUTING_KEY_KEY = "x-dead-letter-routing-key";
	private static final String DLX_TTL_KEY = "x-message-ttl";

	private final RabbitAdmin rabbitAdmin;
	private final RabbitTemplate rabbitTemplate;
	private BeanExpressionResolverDelegator beanExpressionResolverDelegator;

	public FakeAmqpComponentDeclarer(RabbitAdmin rabbitAdmin, RabbitTemplate rabbitTemplate) {
		this.rabbitAdmin = rabbitAdmin;
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanExpressionResolverDelegator = beanFactory.getBean(BeanExpressionResolverDelegator.class);

	}

	public boolean queueDeclarePassive(String queueName) {
		try {
			return rabbitTemplate.execute(channel -> {
				channel.queueDeclarePassive(queueName);
				return Boolean.TRUE;
			});
		} catch (Exception e) {
			//ignore
		}
		return Boolean.FALSE;
	}

	public void declareDlxBinding(DlxDeclarationMetadata dlxDeclarationMetadata) {
		Assert.hasText(dlxDeclarationMetadata.getQueueName(), "QueueName must be set!");
		Assert.hasText(dlxDeclarationMetadata.getExchangeType(), "ExchangeType must be set!");
		if (null == dlxDeclarationMetadata.getExchange()) {
			dlxDeclarationMetadata.setExchange(dlxDeclarationMetadata.getQueueName());
		}
		if (null == dlxDeclarationMetadata.getExchangeType()) {
			dlxDeclarationMetadata.setExchangeType(ExchangeTypes.DIRECT);
		}
		Assert.hasText(dlxDeclarationMetadata.getDlxExchange(), "DlxExchange must be set!");
		Assert.hasText(dlxDeclarationMetadata.getDlxRoutingKey(), "DlxRoutingKey must be set!");
		Assert.notNull(dlxDeclarationMetadata.getDlxQueue(), "DlxQueue must be set!");
		Assert.notNull(dlxDeclarationMetadata.getTtl(), "Ttl must be set!");
		Map<String, Object> arguments = dlxDeclarationMetadata.getArguments();
		if (null == arguments) {
			arguments = Maps.newHashMap();
		}
		arguments.put(DLX_EXCHANGE_KEY, dlxDeclarationMetadata.getDlxExchange());
		arguments.put(DLX_ROUTING_KEY_KEY, dlxDeclarationMetadata.getDlxRoutingKey());
		arguments.put(DLX_TTL_KEY, dlxDeclarationMetadata.getTtl());
		//declare dlx queue
		DirectExchange dlxExchange = new DirectExchange(dlxDeclarationMetadata.getDlxExchange());
		rabbitAdmin.declareExchange(dlxExchange);
		Queue dlxQueue = new Queue(dlxDeclarationMetadata.getDlxQueue(), DEFAULT_DURABLE, DEFAULT_EXCLUSIVE,
				DEFAULT_AUTO_DELETE, null);
		rabbitAdmin.declareQueue(dlxQueue);
		rabbitAdmin.declareBinding(BindingBuilder.bind(dlxQueue).to(dlxExchange).with(dlxDeclarationMetadata.getDlxRoutingKey()));
		//declare target queue
		declareBinding(dlxDeclarationMetadata);
		log.info("Declare dlx queue for queueName {},source queue for queueName {}", dlxQueue.getName(), dlxDeclarationMetadata.getQueueName());
	}

	public void declareBinding(DeclarationMetadata declarationMetadata) {
		Assert.hasText(declarationMetadata.getQueueName(), "QueueName must be set!");
		Assert.hasText(declarationMetadata.getExchangeType(), "ExchangeType must be set!");
		if (null == declarationMetadata.getExchange()) {
			declarationMetadata.setExchange(declarationMetadata.getQueueName());
		}
		if (null == declarationMetadata.getExchangeType()) {
			declarationMetadata.setExchangeType(ExchangeTypes.DIRECT);
		}
		if (Boolean.FALSE.equals(queueDeclarePassive(declarationMetadata.getQueueName()))) {
			Queue queue = new Queue(declarationMetadata.getQueueName(), DEFAULT_DURABLE, DEFAULT_EXCLUSIVE,
					DEFAULT_AUTO_DELETE, resolveArguments(declarationMetadata.getArguments()));
			rabbitAdmin.declareQueue(queue);
			declareExchangeAndBinding(declarationMetadata);
			log.info("Declare common queue for queueName {}", queue.getName());
		}
	}

	public void declareExchangeAndBinding(DeclarationMetadata declarationMetadata) {
		Exchange exchange;
		Binding actualBinding;
		switch (declarationMetadata.getExchangeType().toLowerCase()) {
			case ExchangeTypes.DIRECT:
				exchange = directExchange(declarationMetadata.getExchange(), declarationMetadata.getArguments());
				actualBinding = new Binding(declarationMetadata.getQueueName(), Binding.DestinationType.QUEUE,
						declarationMetadata.getExchange(), declarationMetadata.getRoutingKey(),
						resolveArguments(declarationMetadata.getArguments()));
				break;
			case ExchangeTypes.FANOUT:
				exchange = fanoutExchange(declarationMetadata.getExchange(), declarationMetadata.getArguments());
				actualBinding = new Binding(declarationMetadata.getQueueName(), Binding.DestinationType.QUEUE,
						declarationMetadata.getExchange(), EMPTY, resolveArguments(declarationMetadata.getArguments()));
				break;
			case ExchangeTypes.TOPIC:
				exchange = topicExchange(declarationMetadata.getExchange(), declarationMetadata.getArguments());
				actualBinding = new Binding(declarationMetadata.getQueueName(), Binding.DestinationType.QUEUE,
						declarationMetadata.getExchange(), declarationMetadata.getRoutingKey(),
						resolveArguments(declarationMetadata.getArguments()));
				break;
			case ExchangeTypes.HEADERS:
				exchange = headersExchange(declarationMetadata.getExchange(), declarationMetadata.getArguments());
				actualBinding = new Binding(declarationMetadata.getQueueName(), Binding.DestinationType.QUEUE,
						declarationMetadata.getExchange(), declarationMetadata.getRoutingKey(),
						resolveArguments(declarationMetadata.getArguments()));
				break;
			default: {
				throw new IllegalArgumentException(String.format("Exchange type invalid for value %s", declarationMetadata.getExchangeType()));
			}
		}
		rabbitAdmin.declareExchange(exchange);
		rabbitAdmin.declareBinding(actualBinding);
	}

	private Exchange directExchange(String exchangeName, Map<String, Object> arguments) {
		return new DirectExchange(exchangeName, DEFAULT_DURABLE, DEFAULT_AUTO_DELETE, resolveArguments(arguments));
	}

	private Exchange fanoutExchange(String exchangeName, Map<String, Object> arguments) {
		return new FanoutExchange(exchangeName, DEFAULT_DURABLE, DEFAULT_AUTO_DELETE, resolveArguments(arguments));
	}

	private Exchange topicExchange(String exchangeName, Map<String, Object> arguments) {
		return new TopicExchange(exchangeName, DEFAULT_DURABLE, DEFAULT_AUTO_DELETE, resolveArguments(arguments));
	}

	private Exchange headersExchange(String exchangeName, Map<String, Object> arguments) {
		return new HeadersExchange(exchangeName, DEFAULT_DURABLE, DEFAULT_AUTO_DELETE, resolveArguments(arguments));
	}

	private Map<String, Object> resolveArguments(Map<String, Object> arguments) {
		if (null == arguments) {
			return null;
		}
		Map<String, Object> result = Maps.newHashMapWithExpectedSize(arguments.size());
		for (Map.Entry<String, Object> entry : arguments.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				result.put(beanExpressionResolverDelegator.resolveExpressionAsString(entry.getKey(), "Key"),
						beanExpressionResolverDelegator.resolveExpression((String) value));
			} else {
				result.put(beanExpressionResolverDelegator.resolveExpressionAsString(entry.getKey(), "Key"), value);
			}
		}
		return result;
	}
}
