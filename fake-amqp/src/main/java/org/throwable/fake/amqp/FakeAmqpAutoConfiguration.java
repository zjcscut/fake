package org.throwable.fake.amqp;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.throwable.fake.amqp.common.AmqpConstant;
import org.throwable.fake.amqp.support.*;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 10:35
 */
@EnableConfigurationProperties(value = FakeAmqpProperties.class)
@Configuration
public class FakeAmqpAutoConfiguration implements BeanFactoryAware, SmartInitializingSingleton {

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

    @Bean(name = AmqpConstant.AMQP_ENDPOINT_REGISTRAR_BEAN_NAME)
    public FakeAmqpListenerEndpointRegistrar fakeAmqpListenerEndpointRegistrar(FakeAmqpListenerEndpointRegistry fakeAmqpListenerEndpointRegistry,
                                                                               FakeAmqpHandlerMethodFactoryAdapter fakeAmqpHandlerMethodFactoryAdapter) {
        FakeAmqpListenerEndpointRegistrar registrar = new FakeAmqpListenerEndpointRegistrar();
        registrar.setEndpointRegistry(fakeAmqpListenerEndpointRegistry);
        Map<String, FakeAmqpListenerConfigurer> configurerMap = this.beanFactory.getBeansOfType(FakeAmqpListenerConfigurer.class);
        if (null != configurerMap && !configurerMap.isEmpty()) {
            for (FakeAmqpListenerConfigurer configurer : configurerMap.values()) {
                configurer.configureRabbitListeners(registrar);
            }
        }
        MessageHandlerMethodFactory handlerMethodFactory = registrar.getMessageHandlerMethodFactory();
        if (null != handlerMethodFactory) {
            fakeAmqpHandlerMethodFactoryAdapter.setMessageHandlerMethodFactory(handlerMethodFactory);
        }
        return registrar;
    }

    @Bean(name = AmqpConstant.AMQP_HANDLER_METHOD_FACTORY_ADAPTER_BEAN_NAME)
    public FakeAmqpHandlerMethodFactoryAdapter fakeAmqpHandlerMethodFactoryAdapter() {
        return new FakeAmqpHandlerMethodFactoryAdapter();
    }

    @Bean(name = AmqpConstant.BEAN_EXPRESSION_RESOLVER_DELEGATOR_BEAN_NAME)
    public BeanExpressionResolverDelegator beanExpressionResolverDelegator(){
        return new BeanExpressionResolverDelegator();
    }

    @Bean
    @ConditionalOnMissingBean
    public CachingConnectionFactory cachingConnectionFactory() {
        Assert.hasText(fakeAmqpProperties.getHost(), "Host must be set!");
        Assert.notNull(fakeAmqpProperties.getPort(), "Port must be set!");
        Assert.hasText(fakeAmqpProperties.getUsername(), "Username must be set!");
        Assert.hasText(fakeAmqpProperties.getPassword(), "Password must be set!");
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(fakeAmqpProperties.getHost());
        factory.setPort(fakeAmqpProperties.getPort());
        factory.setUsername(fakeAmqpProperties.getUsername());
        factory.setPassword(fakeAmqpProperties.getPassword());
        if (StringUtils.hasText(fakeAmqpProperties.getVirtualHost())) {
            factory.setVirtualHost(fakeAmqpProperties.getVirtualHost());
        }
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory cachingConnectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(cachingConnectionFactory);
        return rabbitTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitAdmin rabbitAdmin(CachingConnectionFactory cachingConnectionFactory) {
        return new RabbitAdmin(cachingConnectionFactory);
    }

    @Bean(name = AmqpConstant.AMQP_DECLARER_BEAN_NAME)
    @ConditionalOnBean(value = {RabbitTemplate.class, RabbitAdmin.class})
    public FakeAmqpComponentDeclarer fakeAmqpComponentDeclarer(RabbitTemplate rabbitTemplate,
                                                           RabbitAdmin rabbitAdmin) {
        return new FakeAmqpComponentDeclarer(rabbitAdmin, rabbitTemplate);
    }

    @Bean
    @ConditionalOnBean(value = RabbitTemplate.class)
    public FakeAmqpHealthIndicator fakeAmqpHealthIndicator(RabbitTemplate rabbitTemplate) {
        return new FakeAmqpHealthIndicator(rabbitTemplate);
    }

    @Bean(name = AmqpConstant.AMQP_COMPONENT_REGISTRAR_BEAN_NAME)
    public FakeAmqpComponentRegistrar fakeAmqpComponentRegistrar(FakeAmqpComponentDeclarer fakeAmqpComponentDeclarer,
                                                                 FakeAmqpListenerEndpointRegistrar fakeAmqpListenerEndpointRegistrar){
        return new FakeAmqpComponentRegistrar(fakeAmqpComponentDeclarer, fakeAmqpListenerEndpointRegistrar, fakeAmqpProperties);
    }

    /**
     * 1:register all amqp components
     * 2:register all endpoints
     */
    @Override
    public void afterSingletonsInstantiated() {
        processRegisteringAllAmqpComponents();
        processStartingAllEndPoints();
    }

    private void processRegisteringAllAmqpComponents(){
        FakeAmqpComponentRegistrar fakeAmqpComponentRegistrar =
                this.beanFactory.getBean( AmqpConstant.AMQP_COMPONENT_REGISTRAR_BEAN_NAME,FakeAmqpComponentRegistrar.class);
        fakeAmqpComponentRegistrar.registerAllAmqpComponents();
    }

    private void processStartingAllEndPoints(){
        FakeAmqpListenerEndpointRegistrar endpointRegistrar =
                this.beanFactory.getBean(AmqpConstant.AMQP_ENDPOINT_REGISTRAR_BEAN_NAME, FakeAmqpListenerEndpointRegistrar.class);
        endpointRegistrar.afterPropertiesSet();
        endpointRegistrar.registerAllEndpoints();
    }
}
