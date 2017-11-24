package org.throwable.fake.druid.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.lookup.DataSourceLookup;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description fork from {@link org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource}
 * make key to string type
 * @since 2017/11/23 23:02
 */
public abstract class AbstractFakeRoutingDataSource extends AbstractDataSource implements InitializingBean {

	private Map<String, Object> targetDataSources;

	private Object defaultTargetDataSource;

	private String defaultLookupKey;

	private boolean lenientFallback = true;

	private DataSourceLookup dataSourceLookup = new JndiDataSourceLookup();

	private Map<String, DataSource> resolvedDataSources;

	private DataSource resolvedDefaultDataSource;


	public void setTargetDataSources(Map<String, Object> targetDataSources) {
		this.targetDataSources = targetDataSources;
	}

	public void setDefaultTargetDataSource(String defaultLookupKey, Object defaultTargetDataSource) {
		this.defaultLookupKey = defaultLookupKey;
		this.defaultTargetDataSource = defaultTargetDataSource;
	}

	public void setLenientFallback(boolean lenientFallback) {
		this.lenientFallback = lenientFallback;
	}

	public void setDataSourceLookup(DataSourceLookup dataSourceLookup) {
		this.dataSourceLookup = (dataSourceLookup != null ? dataSourceLookup : new JndiDataSourceLookup());
	}

	@Override
	public void afterPropertiesSet() {
		if (this.targetDataSources == null) {
			throw new IllegalArgumentException("Property 'targetDataSources' is required");
		}
		if (this.defaultTargetDataSource == null) {
			throw new IllegalArgumentException("Property 'defaultTargetDataSource' is required");
		}
		if (this.defaultLookupKey == null) {
			throw new IllegalArgumentException("Property 'defaultLookupKey' is required");
		}
		this.resolvedDataSources = new HashMap<>(this.targetDataSources.size());
		for (Map.Entry<String, Object> entry : this.targetDataSources.entrySet()) {
			String lookupKey = resolveSpecifiedLookupKey(entry.getKey());
			DataSource dataSource = resolveSpecifiedDataSource(entry.getValue());
			this.resolvedDataSources.put(lookupKey, dataSource);
		}
		if (this.defaultTargetDataSource != null) {
			this.resolvedDefaultDataSource = resolveSpecifiedDataSource(this.defaultTargetDataSource);
		}
	}

	protected String resolveSpecifiedLookupKey(String lookupKey) {
		return lookupKey;
	}

	protected DataSource resolveSpecifiedDataSource(Object dataSource) throws IllegalArgumentException {
		if (dataSource instanceof DataSource) {
			return (DataSource) dataSource;
		} else if (dataSource instanceof String) {
			return this.dataSourceLookup.getDataSource((String) dataSource);
		} else {
			throw new IllegalArgumentException(
					"Illegal data source value - only [javax.sql.DataSource] and String supported: " + dataSource);
		}
	}

	@Override
	public Connection getConnection() throws SQLException {
		return determineTargetDataSource().getConnection();
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return determineTargetDataSource().getConnection(username, password);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> interfaceToWrap) throws SQLException {
		if (interfaceToWrap.isInstance(this)) {
			return (T) this;
		}
		return determineTargetDataSource().unwrap(interfaceToWrap);
	}

	@Override
	public boolean isWrapperFor(Class<?> interfaceToWrap) throws SQLException {
		return (interfaceToWrap.isInstance(this) || determineTargetDataSource().isWrapperFor(interfaceToWrap));
	}

	protected DataSource determineTargetDataSource() {
		Assert.notNull(this.resolvedDataSources, "DataSource router not initialized");
		String lookupKey = determineCurrentLookupKey();
		DataSource dataSource = this.resolvedDataSources.get(lookupKey);
		if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
			dataSource = this.resolvedDefaultDataSource;
		}
		if (dataSource == null) {
			throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
		}
		return dataSource;
	}

	public Set<String> getLookupKeys() {
		if (null == this.targetDataSources) {
			return null;
		}
		return Collections.unmodifiableSet(this.targetDataSources.keySet());
	}

	public String getDefaultLookupKey() {
		return defaultLookupKey;
	}

	protected abstract String determineCurrentLookupKey();
}
