package org.throwable.fake.amqp.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.throwable.fake.amqp.FakeAmqpProperties;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/28 12:32
 */
public final class FakeAmqpComponentRegistrar implements BeanFactoryAware{

    private final FakeAmqpComponentDeclarer fakeAmqpComponentDeclarer;
    private final FakeAmqpListenerEndpointRegistrar fakeAmqpListenerEndpointRegistrar;
    private final FakeAmqpProperties fakeAmqpProperties;
    private DefaultListableBeanFactory beanFactory;

    public FakeAmqpComponentRegistrar(FakeAmqpComponentDeclarer fakeAmqpComponentDeclarer,
                                      FakeAmqpListenerEndpointRegistrar fakeAmqpListenerEndpointRegistrar,
                                      FakeAmqpProperties fakeAmqpProperties) {
        this.fakeAmqpComponentDeclarer = fakeAmqpComponentDeclarer;
        this.fakeAmqpListenerEndpointRegistrar = fakeAmqpListenerEndpointRegistrar;
        this.fakeAmqpProperties = fakeAmqpProperties;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    public void registerAllAmqpComponents() {
        registerAllCommonQueues();
        registerAllDlxQueues();
        registerAllListeners();
    }

    public void registerAllCommonQueues() {
        if (null != fakeAmqpProperties.getQueues() && !fakeAmqpProperties.getQueues().isEmpty()) {
            for (Map.Entry<String, FakeAmqpProperties.QueueConfigurationPair> entry : fakeAmqpProperties.getQueues().entrySet()) {
                FakeAmqpProperties.QueueConfigurationPair queue = entry.getValue();
                DeclarationMetadata metadata = new DeclarationMetadata.Builder()
                        .setQueueName(queue.getQueueName())
                        .setExchange(queue.getExchangeName())
                        .setExchangeType(queue.getExchangeType())
                        .setRoutingKey(queue.getRoutingKey())
                        .setArguments(queue.getArguments())
                        .build();
                fakeAmqpComponentDeclarer.declareBinding(metadata);
            }
        }
    }

    public void registerAllDlxQueues() {
        if (null != fakeAmqpProperties.getDlx() && !fakeAmqpProperties.getDlx().isEmpty()) {
            for (Map.Entry<String, FakeAmqpProperties.DlxQueueConfigurationPair> entry : fakeAmqpProperties.getDlx().entrySet()) {
                FakeAmqpProperties.DlxQueueConfigurationPair dlx = entry.getValue();
                DlxDeclarationMetadata metadata = new DlxDeclarationMetadata.Builder()
                        .setQueueName(dlx.getQueueName())
                        .setExchange(dlx.getExchangeName())
                        .setExchangeType(dlx.getExchangeType())
                        .setRoutingKey(dlx.getRoutingKey())
                        .setArguments(dlx.getArguments())
                        .setDlxQueue(dlx.getDlxQueue())
                        .setDlxExchange(dlx.getDlxExchange())
                        .setDlxRoutingKey(dlx.getDlxRoutingKey())
                        .build();
                fakeAmqpComponentDeclarer.declareDlxBinding(metadata);
            }
        }
    }

    public void registerAllListeners() {
        if (null != fakeAmqpProperties.getListeners() && !fakeAmqpProperties.getListeners().isEmpty()) {
            for (Map.Entry<String, FakeAmqpProperties.ListenerConfigurationPair> entry : fakeAmqpProperties.getListeners().entrySet()) {
                FakeAmqpProperties.ListenerConfigurationPair value = entry.getValue();

            }
        }
    }

    private void checkListenerParameters(FakeAmqpProperties.ListenerConfigurationPair pair){

    }
}
