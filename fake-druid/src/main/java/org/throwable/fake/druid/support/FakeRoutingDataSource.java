package org.throwable.fake.druid.support;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/23 22:52
 */
public class FakeRoutingDataSource extends AbstractFakeRoutingDataSource implements InitializingBean{

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
	}

	@Override
	protected String determineCurrentLookupKey() {
		return DataSourceLookupKeyHolder.getLookupKey();
	}
}
