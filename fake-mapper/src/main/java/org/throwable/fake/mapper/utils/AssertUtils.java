package org.throwable.fake.mapper.utils;

import java.util.Collection;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/16 23:41
 */
public abstract class AssertUtils {

	public static <T> void notNull(T t, String message, Object... args) {
		if (null == t) {
			throw new IllegalArgumentException(String.format(message, args));
		}
	}

	public static <T> void notEmpty(T[] t, String message, Object... args) {
		if (null == t || t.length == 0) {
			throw new IllegalArgumentException(String.format(message, args));
		}
	}

	public static void notEmpty(Collection<?> collection, String message, Object... args) {
		if (null == collection || collection.isEmpty()) {
			throw new IllegalArgumentException(String.format(message, args));
		}
	}

	public static void notEmpty(Map map, String message, Object... args) {
		if (null == map || map.isEmpty()) {
			throw new IllegalArgumentException(String.format(message, args));
		}
	}


	public static void isTrue(boolean expression, String message, Object... args) {
		if (!expression) {
			throw new IllegalArgumentException(String.format(message, args));
		}
	}

	public static void notBlank(String target, String message, Object... args) {
		if (StringUtils.isBlank(target)) {
			throw new IllegalArgumentException(String.format(message, args));
		}
	}
}
