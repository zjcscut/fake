package org.throwable.fake.mapper;

import lombok.NonNull;
import org.apache.ibatis.annotations.InsertProvider;
import org.throwable.fake.mapper.common.constant.Constants;
import org.throwable.fake.mapper.support.plugins.generator.identity.KeyGeneratorProcessor;
import org.throwable.fake.mapper.support.provider.InsertMapperProvider;


/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/16 23:21
 */
public interface InsertMapper<P,T> {

	default int insert(@NonNull T t) {
		KeyGeneratorProcessor.process(t.getClass(), t);
		return insertInternal(t);
	}

	@Deprecated
	@InsertProvider(type = InsertMapperProvider.class, method = Constants.FAKE_METHOD)
	int insertInternal(@NonNull T t);

	default int insertSkipNull(@NonNull T t) {
		KeyGeneratorProcessor.process(t.getClass(), t);
		return insertSkipNullInternal(t);
	}

	@Deprecated
	@InsertProvider(type = InsertMapperProvider.class, method = Constants.FAKE_METHOD)
	int insertSkipNullInternal(@NonNull T t);


	default int insertIgnore(@NonNull T t) {
		KeyGeneratorProcessor.process(t.getClass(), t);
		return insertIgnoreInternal(t);
	}

	@Deprecated
	@InsertProvider(type = InsertMapperProvider.class, method = Constants.FAKE_METHOD)
	int insertIgnoreInternal(@NonNull T t);

	default int insertIgnoreSkipNull(@NonNull T t) {
		KeyGeneratorProcessor.process(t.getClass(), t);
		return insertIgnoreSkipNullInternal(t);
	}

	@Deprecated
	@InsertProvider(type = InsertMapperProvider.class, method = Constants.FAKE_METHOD)
	int insertIgnoreSkipNullInternal(@NonNull T t);
}
