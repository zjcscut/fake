package org.throwable.fake.mapper;

import lombok.NonNull;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;
import org.throwable.fake.mapper.support.plugins.generator.identity.KeyGeneratorProcessor;
import org.throwable.fake.mapper.support.provider.BatchMapperProvider;

import java.util.List;

import static org.throwable.fake.mapper.common.constant.Constants.*;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/23 11:43
 */
public interface BatchMapper<P,T> {

	default int batchInsert(List<T> list) {
		if (!list.isEmpty()) {
			final Class<?> clazz = list.iterator().next().getClass();
			list.forEach(t -> KeyGeneratorProcessor.process(clazz, t));
		}
		return batchInsertInternal(list);
	}

	@Deprecated
	@InsertProvider(type = BatchMapperProvider.class, method = FAKE_METHOD)
	int batchInsertInternal(@NonNull List<T> list);

	/**
	 * your jdbc configuration field url must be configured with 'allowMultiQueries=true',
	 * for example: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true
	 */
	@UpdateProvider(type = BatchMapperProvider.class, method = FAKE_METHOD)
	int batchUpdateByPrimaryKeyForeach(@NonNull @Param(PARAM_RECORDS) List<T> list, @Param(SKIP_NULL) boolean skipNull);

	@UpdateProvider(type = BatchMapperProvider.class, method = FAKE_METHOD)
	int batchUpdateByPrimaryKeyWhenCase(@NonNull @Param(PARAM_RECORDS) List<T> list, @Param(SKIP_NULL) boolean skipNull);
}
