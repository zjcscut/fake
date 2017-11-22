package org.throwable.fake.mapper.support.plugins.generator.identity;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.throwable.fake.mapper.common.model.ColumnMetadata;
import org.throwable.fake.mapper.support.assistant.SqlAppendMetadataAssistant;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 20:03
 */
public abstract class KeyGeneratorProcessor {

	public static boolean process(Class<?> entityClass, Object target) {
		PrimaryKeyGenerator keyGenerator = PrimaryKeyGeneratorRepository.getKeyGeneratorByEntityClass(entityClass);
		ColumnMetadata keyColumnMetadata = SqlAppendMetadataAssistant.fetchPrimaryColumnMetadata(entityClass);
		if (null != keyColumnMetadata) {
			MetaObject metaObject = SystemMetaObject.forObject(target);
			String keyProperty = keyColumnMetadata.getProperty();
			if (metaObject.hasSetter(keyProperty) && metaObject.hasGetter(keyProperty)) {  //key property must have setter and getter method
				if (null != metaObject.getValue(keyProperty)) { //already has key value
					return true;
				} else if (null != keyGenerator) {
					if (keyGenerator instanceof LongPrimaryKeyGenerator) {
						metaObject.setValue(keyProperty, ((LongPrimaryKeyGenerator) keyGenerator).generatePrimaryKey());
						return true;
					}
					if (keyGenerator instanceof IntegerPrimaryKeyGenerator) {
						metaObject.setValue(keyProperty, ((IntegerPrimaryKeyGenerator) keyGenerator).generatePrimaryKey());
						return true;
					}
					if (keyGenerator instanceof StringPrimaryKeyGenerator) {
						metaObject.setValue(keyProperty, ((StringPrimaryKeyGenerator) keyGenerator).generatePrimaryKey());
						return true;
					}
				}
			}
		}
		return false;
	}

}
