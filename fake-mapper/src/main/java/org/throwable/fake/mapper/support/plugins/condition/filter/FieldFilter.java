package org.throwable.fake.mapper.support.plugins.condition.filter;

import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description 域过滤器
 * @since 2017/3/31 0:01
 */
public interface FieldFilter {

	boolean isIncludeFilter();

	Set<String> accept();
}
