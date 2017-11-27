package org.throwable.fake.amqp.support;

import lombok.Data;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/27 17:10
 */
@Data
public class DeclarationMetadata {

    protected String queueName;
    protected String exchange;
    protected String exchageType;
    protected String routingKey;
    protected Map<String, Object> arguments;
}
