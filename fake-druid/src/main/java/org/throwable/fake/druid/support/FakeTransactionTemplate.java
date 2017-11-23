package org.throwable.fake.druid.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;
import org.throwable.fake.core.utils.AssertUtils;

import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/23 22:50
 */
public class FakeTransactionTemplate extends TransactionTemplate implements InitializingBean {

	private final Set<String> lookupKeys;
	private final String defaultLookupKey;

	public FakeTransactionTemplate(PlatformTransactionManager transactionManager,
								   Set<String> lookupKeys,
								   String defaultLookupKey) {
		super(transactionManager);
		Assert.notNull(transactionManager, "PlatformTransactionManager must not be null!");
		Assert.notNull(lookupKeys, "LookupKeys must not be null!");
		Assert.notNull(defaultLookupKey, "DefaultLookupKey must not be null!");
		this.lookupKeys = lookupKeys;
		this.defaultLookupKey = defaultLookupKey;
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
	}

	public <T> T execute(String lookupKey, TransactionCallback<T> action) throws TransactionException {
		try {
			AssertUtils.assertThrowRuntimeException(lookupKeys.contains(lookupKey),
					() -> new IllegalArgumentException("Invalid lookupKey to execute transaction!"));
			DataSourceLookupKeyHolder.setLookupKey(lookupKey);
			return super.execute(action);
		} finally {
			DataSourceLookupKeyHolder.removeLookupKey();
		}
	}

	@Override
	public <T> T execute(TransactionCallback<T> action) throws TransactionException {
		return this.execute(defaultLookupKey, action);
	}
}
