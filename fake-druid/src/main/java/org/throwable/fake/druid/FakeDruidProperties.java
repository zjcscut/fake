package org.throwable.fake.druid;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Properties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/23 22:46
 */
@Data
@ConfigurationProperties(prefix = FakeDruidProperties.PREFIX)
public class FakeDruidProperties {

	public static final String PREFIX = "fake.druid";

	private String druidAdminUsername;
	private String druidAdminPassword;

	private Map<String, DataSourceProperties> configuration;

	@NoArgsConstructor
	@Data
	public static class DataSourceProperties {

		private Boolean primary = Boolean.FALSE;
		private String url;
		private String driverClassName;
		private String username;
		private String password;
		private Properties properties;
	}
}
