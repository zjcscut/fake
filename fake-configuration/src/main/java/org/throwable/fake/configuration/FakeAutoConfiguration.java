package org.throwable.fake.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.MultiValueMap;
import org.throwable.fake.core.support.BeanDefinitionRegisterAssistor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/16 23:03
 */
@Slf4j
public class FakeAutoConfiguration implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        List<FakeModuleEntranceEnum> availableModuleEnums = getAvailableModuleEnums(importingClassMetadata);
        if (availableModuleEnums.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("Available Fake modules is empty,none Fake features will be provided!");
            }
        } else {
            for (FakeModuleEntranceEnum fakeModuleEntranceEnum : availableModuleEnums) {
                processRegisteringFakeModule(fakeModuleEntranceEnum, registry);
            }
        }
    }

    private List<FakeModuleEntranceEnum> getAvailableModuleEnums(AnnotationMetadata importingClassMetadata) {
        List<FakeModuleEntranceEnum> fakeModuleEntranceEnums = new ArrayList<>();
        MultiValueMap<String, Object> annotationAttributes = importingClassMetadata.getAllAnnotationAttributes(EnableFake.class.getName());
        if (null != annotationAttributes && !annotationAttributes.isEmpty()) {
            LinkedList<Object> modules = new LinkedList<>(annotationAttributes.get("modules"));
            if (!modules.isEmpty()) {
                Object modulesLast = modules.getFirst();
                if (null != modulesLast) {
                    fakeModuleEntranceEnums.addAll(Arrays.asList((FakeModuleEntranceEnum[]) modulesLast));
                }
            }
        }
        return fakeModuleEntranceEnums;
    }

    private void processRegisteringFakeModule(FakeModuleEntranceEnum moduleEntranceEnum, BeanDefinitionRegistry registry) {
        Class<?> targetClass = moduleEntranceEnum.getClassFromName();
        if (null == targetClass) {
            if (log.isWarnEnabled()) {
                log.warn("Skip processing registration for Fake module {},configuration class name {} " +
                                "for configuration class could not be found in the classpath", moduleEntranceEnum.name(),
                        moduleEntranceEnum.getClassName());
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("Process registration for Fake module {},configuration class name {}", moduleEntranceEnum.name(),
                        moduleEntranceEnum.getClassName());
            }
            BeanDefinitionRegisterAssistor.registerBeanDefinitionFromAnnotatedClass(targetClass, registry);
        }
    }
}
