package org.throwable.fake.spring.support;

import org.springframework.boot.actuate.health.HealthAggregator;
import org.springframework.boot.actuate.health.OrderedHealthAggregator;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/26 14:33
 */
public enum AggregatorUtils {

	INSTANCE;

	private static final OrderedHealthAggregator AGGREGATOR = new OrderedHealthAggregator();

	public HealthAggregator getDefaultOrderedHealthAggregator() {
		return AGGREGATOR;
	}
}
