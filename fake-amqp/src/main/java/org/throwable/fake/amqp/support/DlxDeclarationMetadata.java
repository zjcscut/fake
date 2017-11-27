package org.throwable.fake.amqp.support;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/27 17:10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DlxDeclarationMetadata extends DeclarationMetadata {

    protected String dlxQueue;
    protected String dlxExchange;
    protected String dlxRoutingKey;
}
