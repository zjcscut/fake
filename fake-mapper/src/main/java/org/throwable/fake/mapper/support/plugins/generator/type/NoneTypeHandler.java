package org.throwable.fake.mapper.support.plugins.generator.type;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/15 0:33
 */
public class NoneTypeHandler implements TypeHandler<Void> {

	@Override
	public void setParameter(PreparedStatement ps, int i, Void parameter, JdbcType jdbcType) throws SQLException {

	}

	@Override
	public Void getResult(ResultSet rs, String columnName) throws SQLException {
		return null;
	}

	@Override
	public Void getResult(ResultSet rs, int columnIndex) throws SQLException {
		return null;
	}

	@Override
	public Void getResult(CallableStatement cs, int columnIndex) throws SQLException {
		return null;
	}
}
