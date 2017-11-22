package org.throwable.fake.mapper.support.plugins.generator.identity;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/20 2:22
 */
public abstract class PrimaryKeyChecker {

	private static final Map<Class<?>, Class<?>> availableKeyTypeGeneratorMappings = Maps.newHashMapWithExpectedSize(3);

	static {
		availableKeyTypeGeneratorMappings.put(Integer.class, IntegerPrimaryKeyGenerator.class);
		availableKeyTypeGeneratorMappings.put(Long.class, LongPrimaryKeyGenerator.class);
		availableKeyTypeGeneratorMappings.put(String.class, StringPrimaryKeyGenerator.class);
	}

	public static boolean checkPrimaryKeyTypeValid(Class<?> type) {
		return availableKeyTypeGeneratorMappings.containsKey(type);
	}

	public static boolean checkPrimaryKeyGeneratorValid(Class<?> type, Class<? extends PrimaryKeyGenerator> generatorClass) {
		return availableKeyTypeGeneratorMappings.containsKey(type)
				&& generatorClass.isAssignableFrom(availableKeyTypeGeneratorMappings.get(type));
	}
}
