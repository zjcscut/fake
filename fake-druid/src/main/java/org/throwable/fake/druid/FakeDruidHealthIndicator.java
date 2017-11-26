package org.throwable.fake.druid;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.sql.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/26 13:50
 */
@Getter
@AllArgsConstructor
public class FakeDruidHealthIndicator extends AbstractHealthIndicator {

	private final String lookupKey;
	private final DruidDataSource druidDataSource;
	private static final String DEFAULT_QUERY = "SELECT 1";

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		builder.withDetail("lookupKey", lookupKey);
		if (null == druidDataSource) {
			builder.withDetail("database", DatabaseDriver.UNKNOWN.name());
			builder.status(Status.DOWN);
		} else {
			builder.withDetail("url", druidDataSource.getUrl());
			Connection connection = null;
			try {
				connection = druidDataSource.getConnection();
				String productName = getDatabaseProductName(connection);
				builder.withDetail("database", productName);
				builder.withDetail("version", getDatabaseProductVersion(connection));
				doDataSourceStatusCheck(builder, connection, productName);
			} catch (Exception e) {
				builder.status(Status.DOWN);
				builder.withDetail("error", e.getMessage());
			} finally {
				if (null != connection) {
					connection.close();
				}
			}
		}
	}

	private String getDatabaseProductName(Connection connection) throws SQLException {
		return connection.getMetaData().getDatabaseProductName();
	}

	private String getDatabaseProductVersion(Connection connection) throws SQLException {
		return connection.getMetaData().getDatabaseProductVersion();
	}

	private void doDataSourceStatusCheck(Health.Builder builder, Connection connection, String productName) throws Exception {
		DatabaseDriver databaseDriver = DatabaseDriver.fromProductName(productName);
		String validationQuery = databaseDriver.getValidationQuery();
		if (!StringUtils.hasText(validationQuery)) {
			validationQuery = DEFAULT_QUERY;
		}
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(validationQuery);
		ResultSetMetaData metaData = resultSet.getMetaData();
		Assert.isTrue(1 == metaData.getColumnCount(),
				String.format("Incorrect column count: expected:%d,actual:%d", 1, metaData.getColumnCount()));
		builder.withDetail("validationQuery", validationQuery);
		while (resultSet.next()) {
			builder.withDetail("validationQueryResult", resultSet.getString(1));
		}
		builder.status(Status.UP);
	}
}
