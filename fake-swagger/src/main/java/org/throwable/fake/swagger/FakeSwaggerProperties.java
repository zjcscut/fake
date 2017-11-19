package org.throwable.fake.swagger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 11:09
 */
@Data
@ConfigurationProperties(prefix = FakeSwaggerProperties.PREFIX)
public class FakeSwaggerProperties {

	public static final String PREFIX = "fake.swagger";
	public static final String TITLE = "Fake auto application documents";
	public static final String DESCRIPTION = "Fake auto application documents";
	public static final String TERMS_OF_SERVICE_URL = "http://throwable.coding.me";
	public static final String CONTACT_NAME = "throwable";
	public static final String VERSION = "1.0";

	private String title = TITLE;
	private String description = DESCRIPTION;
	private String termsOfServiceUrl = TERMS_OF_SERVICE_URL;
	private String contactName = CONTACT_NAME;
	private String contactUrl = TERMS_OF_SERVICE_URL;
	private String contactEmail = "";
	private String version = VERSION;
	private String licence = "";

	private Boolean any;
	private Boolean none;
	private String methodAnnotation;
	private String classAnnotation;
	private String basePackage;
}
