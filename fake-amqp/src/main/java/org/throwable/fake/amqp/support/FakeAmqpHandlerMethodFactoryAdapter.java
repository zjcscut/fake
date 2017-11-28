package org.throwable.fake.amqp.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;

import java.lang.reflect.Method;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/28 12:10
 */
public class FakeAmqpHandlerMethodFactoryAdapter implements BeanFactoryAware, MessageHandlerMethodFactory {

    private DefaultListableBeanFactory beanFactory;

    private MessageHandlerMethodFactory messageHandlerMethodFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    public MessageHandlerMethodFactory getMessageHandlerMethodFactory() {
        if (null == this.messageHandlerMethodFactory){
            this.messageHandlerMethodFactory = createDefaultMessageHandlerMethodFactory();
        }
        return this.messageHandlerMethodFactory;
    }

    public void setMessageHandlerMethodFactory(MessageHandlerMethodFactory messageHandlerMethodFactory) {
        this.messageHandlerMethodFactory = messageHandlerMethodFactory;
    }

    @Override
    public InvocableHandlerMethod createInvocableHandlerMethod(Object bean, Method method) {
        return getMessageHandlerMethodFactory().createInvocableHandlerMethod(bean, method);
    }

    private MessageHandlerMethodFactory createDefaultMessageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory defaultFactory = new DefaultMessageHandlerMethodFactory();
        defaultFactory.setBeanFactory(this.beanFactory);
        defaultFactory.afterPropertiesSet();
        return defaultFactory;
    }
}
