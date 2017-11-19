package org.throwable.fake.core.utils;

import net.sf.cglib.beans.BeanCopier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/17 0:12
 */
public abstract class BeanCopierUtils {

	private static final Map<String, BeanCopier> BEAN_COPIER_CACHE = new HashMap<>();

	public static <S, T> T copy(S source, Class<T> targetClass) throws Exception {
		AssertUtils.assertThrowRuntimeException(null != source, () -> new NullPointerException("Source must not be null!"));
		AssertUtils.assertThrowRuntimeException(null != targetClass, () -> new NullPointerException("TargetClass must not be null!"));
		return convert(source, targetClass);
	}

	public static <S, T> List<T> copyList(List<S> sourceList, Class<T> targetClass) throws Exception {
		AssertUtils.assertThrowRuntimeException(null != sourceList, () -> new NullPointerException("SourceList must not be null!"));
		AssertUtils.assertThrowRuntimeException(null != targetClass, () -> new NullPointerException("TargetClass must not be null!"));
		List<T> targetList = new ArrayList<>();
		if (!sourceList.isEmpty()) {
			for (S source : sourceList) {
				targetList.add(convert(source, targetClass));
			}
		}
		return targetList;
	}

	private static <S, T> T convert(S source, Class<T> targetClass) throws Exception {
		T t = targetClass.newInstance();
		getOrCreateBeanCopier(source.getClass(), targetClass).copy(source, t, null);
		return t;
	}

	private static BeanCopier getOrCreateBeanCopier(Class<?> source, Class<?> target) {
		String cacheKey = generateCacheKey(source, target);
		BeanCopier beanCopier;
		if (BEAN_COPIER_CACHE.containsKey(cacheKey)) {
			beanCopier = BEAN_COPIER_CACHE.get(cacheKey);
		} else {
			beanCopier = BeanCopier.create(source, target, false);
			BEAN_COPIER_CACHE.put(cacheKey, beanCopier);
		}
		return beanCopier;
	}

	private static String generateCacheKey(Class<?> source, Class<?> target) {
		return String.format("%s_%s", source.getName(), target.getName());
	}
}
