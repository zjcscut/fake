package org.throwable.fake.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/26 15:06
 */
@Order
@Configuration
public class FakeEndpointAutoConfiguration {

	@Bean
	public FakeHealthEndpoint fakeHealthEndpoint(){
		return new FakeHealthEndpoint();
	}
}
