package org.throwable.fake.swagger;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/18 10:51
 */
@SuppressWarnings("unchecked")
@EnableSwagger2
@Configuration
@EnableConfigurationProperties(value = FakeSwaggerProperties.class)
public class FakeSwaggerAutoConfiguration implements BeanFactoryAware {

	private DefaultListableBeanFactory beanFactory;
	private final FakeSwaggerProperties fakeSwaggerProperties;

	public FakeSwaggerAutoConfiguration(FakeSwaggerProperties fakeSwaggerProperties) {
		this.fakeSwaggerProperties = fakeSwaggerProperties;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
	}

	@Bean
	@ConditionalOnMissingBean
	public ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title(fakeSwaggerProperties.getTitle())
				.description(fakeSwaggerProperties.getDescription())
				.contact(new Contact(fakeSwaggerProperties.getContactName(),
						fakeSwaggerProperties.getContactUrl(), fakeSwaggerProperties.getContactEmail()))
				.termsOfServiceUrl(fakeSwaggerProperties.getTermsOfServiceUrl())
				.version(fakeSwaggerProperties.getVersion())
				.license(fakeSwaggerProperties.getLicence())
				.build();
	}

	@Bean
	@ConditionalOnMissingBean
	public Docket docket(ApiInfo apiInfo) throws Exception {
		ApiSelectorBuilder apiSelectorBuilder = new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo).select();
		Map<String, FakeSwaggerConfigurer> configurerMap = beanFactory.getBeansOfType(FakeSwaggerConfigurer.class);
		if (null != configurerMap && !configurerMap.isEmpty()) {
			Predicate<RequestHandler> apis = RequestHandlerSelectors.any();
			Predicate<String> paths = PathSelectors.any();
			for (FakeSwaggerConfigurer configurer : configurerMap.values()) {
				Predicate<RequestHandler> handlerPredicate = configurer.configApis();
				if (null != handlerPredicate) {
					apis = Predicates.and(handlerPredicate, apis);
				}
				Predicate<String> predicate = configurer.configPaths();
				if (null != predicate) {
					paths = Predicates.and(predicate, paths);
				}
			}
			apiSelectorBuilder.apis(apis).paths(paths);
		} else {
			apiSelectorBuilder.apis(mergeConfigurableRequestHandler()).paths(PathSelectors.any());
		}
		return apiSelectorBuilder.build();
	}

	private Predicate<RequestHandler> mergeConfigurableRequestHandler() throws Exception {
		Predicate<RequestHandler> defaultRequestHandler = RequestHandlerSelectors.any();
		if (Boolean.TRUE.equals(fakeSwaggerProperties.getAny())) {
			return defaultRequestHandler;
		}
		if (Boolean.TRUE.equals(fakeSwaggerProperties.getNone())) {
			return RequestHandlerSelectors.none();
		}
		if (null != fakeSwaggerProperties.getBasePackage()) {
			defaultRequestHandler = RequestHandlerSelectors.basePackage(fakeSwaggerProperties.getBasePackage());
		}
		if (null != fakeSwaggerProperties.getClassAnnotation()) {
			String[] classAnnotations = fakeSwaggerProperties.getClassAnnotation().split(",");
			for (String classAnnotation : classAnnotations) {
				Predicate<RequestHandler> classAnnotationRequestHandler =
						RequestHandlerSelectors.withClassAnnotation(getConfigurableAnnotationClass(classAnnotation));
				defaultRequestHandler = Predicates.and(defaultRequestHandler, classAnnotationRequestHandler);
			}
		}
		if (null != fakeSwaggerProperties.getMethodAnnotation()) {
			String[] methodAnnotations = fakeSwaggerProperties.getMethodAnnotation().split(",");
			for (String methodAnnotation : methodAnnotations) {
				Predicate<RequestHandler> classAnnotationRequestHandler =
						RequestHandlerSelectors.withClassAnnotation(getConfigurableAnnotationClass(methodAnnotation));
				defaultRequestHandler = Predicates.and(defaultRequestHandler, classAnnotationRequestHandler);
			}
		}
		return defaultRequestHandler;
	}

	private Class<? extends Annotation> getConfigurableAnnotationClass(String className) throws Exception {
		Class<?> clazz = ClassUtils.forName(className, FakeSwaggerAutoConfiguration.class.getClassLoader());
		return (Class<? extends Annotation>) clazz;
	}
}
