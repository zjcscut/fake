package org.throwable.fake.core.support;

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.throwable.fake.core.utils.AssertUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/17 0:35
 */
public final class BeanDefinitionRegisterAssistor {

    private static final BeanNameGenerator ANNOTATED_BEAN_NAME_GENERATOR = new AnnotationBeanNameGenerator();
    private static final BeanNameGenerator DEFAULT_BEAN_NAME_GENERATOR = new DefaultBeanNameGenerator();
    private static final Set<String> AVAILABLE_SCOPES = new HashSet<>();

    static {
        AVAILABLE_SCOPES.add(ConfigurableBeanFactory.SCOPE_SINGLETON);
        AVAILABLE_SCOPES.add(ConfigurableBeanFactory.SCOPE_PROTOTYPE);
    }

    public static void registerBeanDefinitionFromCommonClass(Class<?> targetClass,
                                                             BeanDefinitionRegistry registry) {
        registerBeanDefinitionFromCommonClass(targetClass, registry, ConfigurableBeanFactory.SCOPE_SINGLETON);
    }

    public static void registerBeanDefinitionFromCommonClass(Class<?> targetClass,
                                                             BeanDefinitionRegistry registry,
                                                             String scope) {
        AssertUtils.assertThrowRuntimeException(AVAILABLE_SCOPES.contains(scope),
                () -> new IllegalArgumentException(String.format("Invalid scope %s", scope)));
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(targetClass);
        beanDefinition.setScope(scope);
        BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition,
                DEFAULT_BEAN_NAME_GENERATOR.generateBeanName(beanDefinition, registry));
        BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, registry);
    }

    public static void registerBeanDefinitionFromAnnotatedClass(Class<?> targetClass,
                                                                BeanDefinitionRegistry registry) {
        registerBeanDefinitionFromAnnotatedClass(targetClass, registry, ConfigurableBeanFactory.SCOPE_SINGLETON);
    }

    public static void registerBeanDefinitionFromAnnotatedClass(Class<?> targetClass,
                                                                BeanDefinitionRegistry registry,
                                                                String scope) {
        AssertUtils.assertThrowRuntimeException(AVAILABLE_SCOPES.contains(scope),
                () -> new IllegalArgumentException(String.format("Invalid scope %s", scope)));
        AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(targetClass);
        beanDefinition.setScope(scope);
        AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDefinition);
        BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition,
                ANNOTATED_BEAN_NAME_GENERATOR.generateBeanName(beanDefinition, registry));
        BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, registry);
    }
}
