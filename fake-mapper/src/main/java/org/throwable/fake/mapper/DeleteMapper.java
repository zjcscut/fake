package org.throwable.fake.mapper;

import lombok.NonNull;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Param;
import org.throwable.fake.mapper.common.constant.Constants;
import org.throwable.fake.mapper.support.plugins.condition.Condition;
import org.throwable.fake.mapper.support.provider.DeleteMapperProvider;

import static org.throwable.fake.mapper.common.constant.Constants.FAKE_METHOD;
import static org.throwable.fake.mapper.common.constant.Constants.PARAM_CONDITION;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/16 23:24
 */
public interface DeleteMapper<P,T> {

	@DeleteProvider(type = DeleteMapperProvider.class, method = FAKE_METHOD)
	int deleteByPrimaryKey(@NonNull @Param(Constants.PARAM_PRIMARY_KEY) P key);

	@DeleteProvider(type = DeleteMapperProvider.class, method = FAKE_METHOD)
	int deleteByCondition(@NonNull @Param(PARAM_CONDITION) Condition condition);
}
