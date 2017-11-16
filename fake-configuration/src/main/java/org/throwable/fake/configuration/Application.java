package org.throwable.fake.configuration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/16 23:43
 */
@EnableFake
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}