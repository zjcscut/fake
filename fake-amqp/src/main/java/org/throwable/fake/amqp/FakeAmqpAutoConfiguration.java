package org.throwable.fake.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.throwable.fake.amqp.common.AmqpConstant;
import org.throwable.fake.amqp.support.FakeAmqpListenerAnnotationBeanPostProcessor;
import org.throwable.fake.amqp.support.FakeAmqpListenerEndpointRegistry;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 10:35
 */
@EnableConfigurationProperties(value = FakeAmqpProperties.class)
@Configuration
public class FakeAmqpAutoConfiguration implements BeanFactoryAware,SmartInitializingSingleton {

	private final FakeAmqpProperties fakeAmqpProperties;
	private DefaultListableBeanFactory beanFactory;

	public FakeAmqpAutoConfiguration(FakeAmqpProperties fakeAmqpProperties) {
		this.fakeAmqpProperties = fakeAmqpProperties;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
	}

	@Bean(name = AmqpConstant.AMQP_BEAN_POST_PROCESSOR_BEAN_NAME)
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public FakeAmqpListenerAnnotationBeanPostProcessor fakeAmqpListenerAnnotationBeanPostProcessor() {
		return new FakeAmqpListenerAnnotationBeanPostProcessor();
	}

	@Bean(name = AmqpConstant.AMQP_ENDPOINT_REGISTRY_BEAN_NAME)
	public FakeAmqpListenerEndpointRegistry fakeAmqpListenerEndpointRegistry() {
		return new FakeAmqpListenerEndpointRegistry();
	}

	@Bean
	@ConditionalOnBean(value = RabbitTemplate.class)
	public FakeAmqpHealthIndicator fakeAmqpHealthIndicator(RabbitTemplate rabbitTemplate){
       return new FakeAmqpHealthIndicator(rabbitTemplate);
	}

	@Override
	public void afterSingletonsInstantiated() {

	}
}
