package org.throwable.fake.druid.support;

import org.springframework.core.NamedThreadLocal;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/23 22:58
 */
class DataSourceLookupKeyHolder {

	private final static ThreadLocal<String> LOOKUP_KEY = new NamedThreadLocal<>("Druid Lookup Keys");

	static String getLookupKey() {
		return LOOKUP_KEY.get();
	}

	static void setLookupKey(String lookupKey) {
		LOOKUP_KEY.set(lookupKey);
	}

	static void removeLookupKey() {
		LOOKUP_KEY.remove();
	}
}
