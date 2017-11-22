package org.throwable.fake.mapper;

import lombok.NonNull;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;
import org.throwable.fake.mapper.support.plugins.condition.Condition;
import org.throwable.fake.mapper.support.provider.UpdateMapperProvider;

import static org.throwable.fake.mapper.common.constant.Constants.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/16 23:22
 */
public interface UpdateMapper<P,T> {

	default int updateByPrimaryKey(T t) {
		return updateByPrimaryKey(t, false);
	}

	@UpdateProvider(type = UpdateMapperProvider.class, method = FAKE_METHOD)
	int updateByPrimaryKey(@NonNull @Param(PARAM_RECORD) T t,
                           @Param(PARAM_ALLOW_UPDATE_TO_NULL) boolean allowUpdateToNull);

	@UpdateProvider(type = UpdateMapperProvider.class, method = FAKE_METHOD)
	int updateByCondition(@NonNull @Param(PARAM_CONDITION) Condition condition);
}
