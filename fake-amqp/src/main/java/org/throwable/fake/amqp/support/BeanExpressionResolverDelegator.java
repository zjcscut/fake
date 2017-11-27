package org.throwable.fake.amqp.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/27 16:52
 */
public final class BeanExpressionResolverDelegator implements BeanFactoryAware {

    private DefaultListableBeanFactory beanFactory;
    private BeanExpressionResolver resolver = new StandardBeanExpressionResolver();
    private BeanExpressionContext expressionContext;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
        this.resolver = this.beanFactory.getBeanExpressionResolver();
        this.expressionContext = new BeanExpressionContext(this.beanFactory, null);
    }

    public String resolve(String value) {
        if (this.beanFactory != null) {
            return this.beanFactory.resolveEmbeddedValue(value);
        }
        return value;
    }

    public Object resolveExpression(String value) {
        String resolvedValue = resolve(value);
        if (!(resolvedValue.startsWith("#{") && value.endsWith("}"))) {
            return resolvedValue;
        }
        return this.resolver.evaluate(resolvedValue, this.expressionContext);
    }

    public String resolveExpressionAsString(String value, String attribute) {
        Object resolved = resolveExpression(value);
        if (resolved instanceof String) {
            return (String) resolved;
        } else {
            throw new IllegalStateException("The [" + attribute + "] must resolve to a String. "
                    + "Resolved to [" + resolved.getClass() + "] for [" + value + "]");
        }
    }

    public boolean resolveExpressionAsBoolean(String value) {
        Object resolved = resolveExpression(value);
        if (resolved instanceof Boolean) {
            return (Boolean) resolved;
        } else if (resolved instanceof String) {
            return Boolean.valueOf((String) resolved);
        } else {
            return Boolean.FALSE;
        }
    }
}
