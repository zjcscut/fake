package org.throwable.fake.amqp;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 18:42
 */
@Data
@ConfigurationProperties(prefix = FakeAmqpProperties.PREFIX)
public class FakeAmqpProperties {

    public static final String PREFIX = "fake.amqp";
    private static final int CONCURRENT_CONSUMERS = 10;
    private static final int MAX_CONCURRENT_CONSUMERS = 20;

    private String host;
    private Integer port;
    private String username;
    private String password;
    private String virtualHost;

    private Map<String, QueueConfigurationPair> queues;
    private Map<String, ListenerConfigurationPair> listeners;
    private Map<String, DlxQueueConfigurationPair> dlx;

    @Setter
    @Getter
    public static class QueueConfigurationPair {

        protected String queueName;
        protected String exchangeName;
        protected String exchangeType;
        protected String routingKey;
        protected Map<String, Object> arguments;
    }

    @Setter
    @Getter
    public static class ListenerConfigurationPair extends QueueConfigurationPair {

        protected String group;
        protected String acknowledgeMode;
        protected Integer concurrentConsumers = CONCURRENT_CONSUMERS;
        protected Integer maxConcurrentConsumers = MAX_CONCURRENT_CONSUMERS;
        protected String listenerClassName;
    }

    @Setter
    @Getter
    public static class DlxQueueConfigurationPair extends QueueConfigurationPair {

        protected String dlxQueue;
        protected String dlxExchange;
        protected String dlxRoutingKey;
        /**
         * time(ms) to live
         */
        protected Integer ttl;
    }
}
