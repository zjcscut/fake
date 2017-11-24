package org.throwable.fake.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.throwable.fake.druid.support.DynamicDataSourceAspect;
import org.throwable.fake.druid.support.FakeRoutingDataSource;
import org.throwable.fake.druid.support.FakeTransactionTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/23 22:44
 */
@ConditionalOnClass(DruidDataSource.class)
@EnableConfigurationProperties(value = FakeDruidProperties.class)
@Configuration
public class FakeDruidAutoConfiguration {

	public final FakeDruidProperties fakeDruidProperties;

	public FakeDruidAutoConfiguration(FakeDruidProperties fakeDruidProperties) {
		this.fakeDruidProperties = fakeDruidProperties;
	}

	@Bean
	public FakeRoutingDataSource dataSource() throws Exception {
		validateFakeDruidProperties(fakeDruidProperties);
		FakeRoutingDataSource fakeRoutingDataSource = new FakeRoutingDataSource();
		Map<String, Object> targetDataSources = Maps.newHashMap();
		List<DataSourceHolder> dataSourceHolders = determineTargetDruidDataSources(fakeDruidProperties);
		for (DataSourceHolder dataSourceHolder : dataSourceHolders) {
			targetDataSources.put(dataSourceHolder.getLookupKey(), dataSourceHolder.getDruidDataSource());
		}
		fakeRoutingDataSource.setTargetDataSources(targetDataSources);
		DataSourceHolder defaultDataSource = determinePrimaryDruidDataSources(dataSourceHolders);
		fakeRoutingDataSource.setDefaultTargetDataSource(defaultDataSource.getLookupKey(), defaultDataSource.getDruidDataSource());
		return fakeRoutingDataSource;
	}

	@Bean
	@ConditionalOnMissingBean(value = PlatformTransactionManager.class)
	public PlatformTransactionManager transactionManager(FakeRoutingDataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	@ConditionalOnMissingBean(value = FakeTransactionTemplate.class)
	public FakeTransactionTemplate fakeTransactionTemplate(PlatformTransactionManager transactionManager,
														   FakeRoutingDataSource fakeRoutingDataSource) {
		return new FakeTransactionTemplate(transactionManager, fakeRoutingDataSource.getLookupKeys(),
				fakeRoutingDataSource.getDefaultLookupKey());
	}

	@Bean
	@ConditionalOnMissingBean
	public DynamicDataSourceAspect dynamicDataSourceAspect(FakeRoutingDataSource fakeRoutingDataSource){
		return new DynamicDataSourceAspect(fakeRoutingDataSource.getLookupKeys());
	}

	private void validateFakeDruidProperties(FakeDruidProperties fakeDruidProperties) {
		Map<String, FakeDruidProperties.DataSourceProperties> configuration = fakeDruidProperties.getConfiguration();
		Assert.notEmpty(configuration, "Druid configuration mappings must not be empty!");
		Set<String> keys = configuration.keySet();
		Assert.notEmpty(keys, "Druid configuration mapping keys must not be empty!");
		Assert.isTrue(keys.parallelStream().allMatch(StringUtils::hasText), "Druid configuration mapping key pair must not be blank!");
		Collection<FakeDruidProperties.DataSourceProperties> values = configuration.values();
		Assert.notEmpty(values, "Druid configuration mapping values must not be empty!");
		for (FakeDruidProperties.DataSourceProperties dataSourceProperties : values) {
			Assert.hasText(dataSourceProperties.getUrl(), "DataSourceProperties property 'url' must be set!");
			Assert.hasText(dataSourceProperties.getDriverClassName(), "DataSourceProperties property 'driverClassName' must be set!");
			Assert.hasText(dataSourceProperties.getUsername(), "DataSourceProperties property 'username' must be set!");
			Assert.hasText(dataSourceProperties.getPassword(), "DataSourceProperties property 'password' must be set!");
		}
		Assert.isTrue(1L == values.parallelStream().filter(FakeDruidProperties.DataSourceProperties::getPrimary).count(),
				"Primary druid datasource must be set and only one primary should be set!");
	}

	private List<DataSourceHolder> determineTargetDruidDataSources(FakeDruidProperties fakeDruidProperties) throws Exception {
		List<DataSourceHolder> dataSourceHolders = Lists.newArrayList();
		for (Map.Entry<String, FakeDruidProperties.DataSourceProperties> propertiesEntry : fakeDruidProperties.getConfiguration().entrySet()) {
			dataSourceHolders.add(setUpDataSourceHolder(propertiesEntry.getKey(), propertiesEntry.getValue()));
		}
		return dataSourceHolders;
	}

	private DataSourceHolder setUpDataSourceHolder(String lookupKey, FakeDruidProperties.DataSourceProperties dataSourceProperties) throws Exception {
		return new DataSourceHolder.Builder()
				.setPrimary(dataSourceProperties.getPrimary())
				.setLookupKey(lookupKey)
				.setDruidDataSource(setUpDruidDataSource(dataSourceProperties))
				.build();
	}

	private DruidDataSource setUpDruidDataSource(FakeDruidProperties.DataSourceProperties dataSourceProperties) throws Exception {
		DruidDataSource druidDataSource;
		if (null != dataSourceProperties.getProperties()) {
			druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(dataSourceProperties.getProperties());
		} else {
			druidDataSource = new DruidDataSource();
		}
		druidDataSource.setUrl(dataSourceProperties.getUrl());
		druidDataSource.setDriverClassName(dataSourceProperties.getDriverClassName());
		druidDataSource.setUsername(dataSourceProperties.getUsername());
		druidDataSource.setPassword(dataSourceProperties.getPassword());
		return druidDataSource;
	}

	private DataSourceHolder determinePrimaryDruidDataSources(List<DataSourceHolder> dataSourceHolders) {
		return dataSourceHolders.stream()
				.filter(DataSourceHolder::getPrimary)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("None primary druid datasource has been set!"));
	}

	@Setter
	@Getter
	private static class DataSourceHolder {

		private final Boolean primary;
		private final String lookupKey;
		private final DruidDataSource druidDataSource;

		public DataSourceHolder(Builder builder) {
			this.primary = builder.primary;
			this.lookupKey = builder.lookupKey;
			this.druidDataSource = builder.druidDataSource;
		}

		public Boolean getPrimary() {
			return primary;
		}

		public String getLookupKey() {
			return lookupKey;
		}

		public DruidDataSource getDruidDataSource() {
			return druidDataSource;
		}

		public static class Builder {
			private Boolean primary;
			private String lookupKey;
			private DruidDataSource druidDataSource;

			public Builder setPrimary(Boolean primary) {
				this.primary = primary;
				return this;
			}

			public Builder setLookupKey(String lookupKey) {
				this.lookupKey = lookupKey;
				return this;
			}

			public Builder setDruidDataSource(DruidDataSource druidDataSource) {
				this.druidDataSource = druidDataSource;
				return this;
			}

			public DataSourceHolder build() {
				return new DataSourceHolder(this);
			}
		}
	}
}
