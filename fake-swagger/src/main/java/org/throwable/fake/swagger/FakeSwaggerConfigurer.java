package org.throwable.fake.swagger;

import com.google.common.base.Predicate;
import springfox.documentation.RequestHandler;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 11:43
 */
public abstract class FakeSwaggerConfigurer {

	protected Predicate<RequestHandler> configApis() {
		return null;
	}

	protected Predicate<String> configPaths() {
		return null;
	}
}
