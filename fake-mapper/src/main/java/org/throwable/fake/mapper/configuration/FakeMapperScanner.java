package org.throwable.fake.mapper.configuration;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.apache.ibatis.annotations.Mapper;
import org.reflections.Reflections;
import org.reflections.scanners.*;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/16 23:38
 */
public class FakeMapperScanner {

	@Getter
	private String[] packages;
	@Getter
	private Set<Class<? extends Annotation>> includeAnnotations = Sets.newHashSetWithExpectedSize(1);

	private Reflections reflections;

	public FakeMapperScanner(String[] packages) {
		this.packages = packages;
		registerDefaultIncludeAnnotations();
		registerDefaultReflections();
	}

	private void registerDefaultIncludeAnnotations() {
		this.includeAnnotations.add(Mapper.class);
	}

	private void registerDefaultReflections() {
		this.reflections = new Reflections(
				new ConfigurationBuilder()
						.forPackages(packages)
						.setScanners(
								new SubTypesScanner(),
								new TypeAnnotationsScanner(),
								new MethodAnnotationsScanner(),
								new FieldAnnotationsScanner(),
								new MethodParameterScanner()));
	}

	public void setIncludeAnnotations(Set<Class<? extends Annotation>> includeAnnotations) {
		this.includeAnnotations.clear();
		this.includeAnnotations.addAll(includeAnnotations);
	}

	public Set<Class<?>> doScan() {
		Set<Class<?>> result = new HashSet<>();
		for (Class<? extends Annotation> annotation : this.includeAnnotations) {
			result.addAll(this.reflections.getTypesAnnotatedWith(annotation));
		}
		return result;
	}
}
