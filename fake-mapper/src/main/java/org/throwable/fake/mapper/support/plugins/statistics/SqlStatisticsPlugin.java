package org.throwable.fake.mapper.support.plugins.statistics;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.throwable.fake.mapper.utils.CollectionUtils;
import org.throwable.fake.mapper.utils.StringUtils;

import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/24 15:20
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
		@Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
		@Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})})
public class SqlStatisticsPlugin implements Interceptor {

	private static final Log log = LogFactory.getLog(SqlStatisticsPlugin.class);

	//TODO  to finish intercept method for sql statistics
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		Object target = invocation.getTarget();
		if (target instanceof StatementHandler) {
			StatementHandler statementHandler = (StatementHandler) target;
			long startTime = System.currentTimeMillis();
			try {
				return invocation.proceed();
			} finally {
				long endTime = System.currentTimeMillis();
				long costTime = endTime - startTime;
				processRecordingSqlStatistics(costTime, statementHandler);
			}
		} else {
			return invocation.proceed();
		}
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {

	}

	private void processRecordingSqlStatistics(long costTime, StatementHandler statementHandler) {
		BoundSql boundSql = statementHandler.getBoundSql();
		String sql = boundSql.getSql();
		sql = beautifySql(sql);
		printSql(sql, costTime);
	}

	private String processSqlWithParameters(String originSql, Object parameterObject, List<ParameterMapping> parameterMappings) {
		//blank sql,return empty string.
		if (StringUtils.isBlank(originSql)) {
			return StringUtils.EMPTY;
		}
		originSql = beautifySql(originSql);
		//none parameter,return origin sql.
		if (null == parameterObject || CollectionUtils.isNullOrEmpty(parameterMappings)) {
			return originSql;
		}
		String originSqlHolder = originSql;
		try {
			return processHandlingParameterMapping(originSql, parameterObject, parameterMappings);
		} catch (Exception e) {
			//ignore
		}
		return originSqlHolder;
	}

	private String processHandlingParameterMapping(String originSql, Object parameterObject, List<ParameterMapping> parameterMappings) throws Exception {
         if (!CollectionUtils.isNullOrEmpty(parameterMappings)){
         	Class<?> parameterClass = parameterObject.getClass();

		 }
		 return originSql;
	}

	private String beautifySql(String sql) {
		return sql.replaceAll("[\\s\n ]+", " ");
	}

	private void printSql(String sql, long costTime) {
		if (log.isDebugEnabled()) {
			log.debug(String.format("Sql:[%s],costTime:[%s ms]", sql, costTime));
		}
	}
}
