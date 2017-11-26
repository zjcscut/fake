package org.throwable.fake.amqp;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/26 15:36
 */
public class FakeAmqpHealthIndicator extends AbstractHealthIndicator {

	private final RabbitTemplate rabbitTemplate;

	public FakeAmqpHealthIndicator(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();
		if (null != connectionFactory) {
			builder.withDetail("host", connectionFactory.getHost());
			builder.withDetail("port", connectionFactory.getPort());
		}
		builder.up().withDetail("version", getRabbitmqVersion());
	}

	private String getRabbitmqVersion() {
		return this.rabbitTemplate.execute(channel -> {
			Map<String, Object> serverProperties = channel.getConnection()
					.getServerProperties();
			return serverProperties.get("version").toString();
		});
	}
}
