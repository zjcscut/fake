package org.throwable.fake.amqp.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.ContentTypeDelegatingMessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.throwable.fake.amqp.common.AmqpConstant;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/28 15:26
 */
@Slf4j
public abstract class FakeAmqpComponentUtils {

    private static final AmqpMessageConverterFactory CONVERTER_FACTORY = new ContentTypeDelegatingConverterFactory();
    private static final ContentTypeDelegatingMessageConverter CONVERTER;

    static {
        CONVERTER = (ContentTypeDelegatingMessageConverter) CONVERTER_FACTORY.createMessageConverter();
        CONVERTER.addDelegate(MediaType.APPLICATION_JSON_VALUE, new Jackson2JsonMessageConverter());
        CONVERTER.addDelegate(MediaType.APPLICATION_JSON_UTF8_VALUE, new Jackson2JsonMessageConverter());
    }


    public static CachingConnectionFactory getExistingCachingConnectionFactory(BeanFactory beanFactory) {
        try {
            return beanFactory.getBean(CachingConnectionFactory.class);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new BeanInitializationException("No " + CachingConnectionFactory.class.getSimpleName() +
                    " was found in the spring application context", ex);
        }
    }

    public static RabbitAdmin getExistingRabbitAdmin(BeanFactory beanFactory) {
        try {
            return beanFactory.getBean(RabbitAdmin.class);
        } catch (NoSuchBeanDefinitionException ex) {
            throw new BeanInitializationException("No " + RabbitAdmin.class.getSimpleName() +
                    " was found in the spring application context", ex);
        }
    }

    public static SimpleRabbitListenerContainerFactory getExistingRabbitListenerContainerFactory(BeanFactory beanFactory) {
        Assert.isTrue(beanFactory instanceof DefaultListableBeanFactory, "BeanFactory instance must be DefaultListableBeanFactory!");
        String containerFactoryBeanName = AmqpConstant.AMQP_CONTAINER_FACTORY_BEAN_NAME;
        SimpleRabbitListenerContainerFactory containerFactory;
        if (!beanFactory.containsBean(containerFactoryBeanName)) {
            CachingConnectionFactory connectionFactory = getExistingCachingConnectionFactory(beanFactory);
            SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer
                    = beanFactory.getBean(SimpleRabbitListenerContainerFactoryConfigurer.class);
            containerFactory = new SimpleRabbitListenerContainerFactory();
            factoryConfigurer.configure(containerFactory, connectionFactory);
            containerFactory.setConnectionFactory(connectionFactory);
            containerFactory.setAcknowledgeMode(AcknowledgeMode.NONE);
            containerFactory.setMessageConverter(CONVERTER);
            ((DefaultListableBeanFactory) beanFactory).registerSingleton(containerFactoryBeanName, containerFactory);
        } else {
            containerFactory = beanFactory.getBean(containerFactoryBeanName, SimpleRabbitListenerContainerFactory.class);
        }
        return containerFactory;
    }

    public static MessageConverter getContentTypeDelegatingMessageConverter() {
        return CONVERTER;
    }
}
