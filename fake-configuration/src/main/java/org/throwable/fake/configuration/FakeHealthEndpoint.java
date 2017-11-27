package org.throwable.fake.configuration;

import com.google.common.collect.Maps;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.actuate.health.CompositeHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.util.StringUtils;
import org.throwable.fake.spring.support.AggregatorUtils;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/26 15:11
 */
public class FakeHealthEndpoint extends AbstractEndpoint<Health> implements BeanFactoryAware {

	private DefaultListableBeanFactory beanFactory;

	public FakeHealthEndpoint() {
		super("fakeHealth", Boolean.FALSE, Boolean.TRUE);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
	}

	@Override
	public Health invoke() {
		Map<String, HealthIndicator> indicators = Maps.newHashMap();
		Map<String, HealthIndicator> healthIndicatorMap = beanFactory.getBeansOfType(HealthIndicator.class);
		if (null != healthIndicatorMap && !healthIndicatorMap.isEmpty()) {
			for (Map.Entry<String, HealthIndicator> entry : healthIndicatorMap.entrySet()) {
				if (StringUtils.hasText(entry.getKey()) && entry.getKey().startsWith("fake")) {
					indicators.put(entry.getKey(), entry.getValue());
				}
			}
		}
		CompositeHealthIndicator compositeHealthIndicator =
				new CompositeHealthIndicator(AggregatorUtils.INSTANCE.getDefaultOrderedHealthAggregator(), indicators);
		return compositeHealthIndicator.health();
	}
}
