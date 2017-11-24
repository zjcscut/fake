package org.throwable.fake.druid.support;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.throwable.fake.druid.annotation.DynamicDataSource;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/24 9:47
 */
@Aspect
public class DynamicDataSourceAspect {

    private final Set<String> lookupKeys;

    public DynamicDataSourceAspect(Set<String> lookupKeys) {
        this.lookupKeys = lookupKeys;
    }

    @Pointcut("@annotation(org.throwable.fake.druid.annotation.DynamicDataSource)")
    public void dynamicDataSourcePointcut() {

    }

    @Around("dynamicDataSourcePointcut()")
    public Object methodsAnnotatedWithDynamicDataSource(final ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            //获取目标方法,有可能是接口方法
            Method targetMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
            //注解必须放在实现方法
            Method currentMethod = joinPoint.getTarget().getClass().getDeclaredMethod(targetMethod.getName(), targetMethod.getParameterTypes());
            DynamicDataSource dynamicDataSource = AnnotationUtils.findAnnotation(currentMethod, DynamicDataSource.class);
            String lookupKey = dynamicDataSource.lookupKey();
            Assert.isTrue(lookupKeys.contains(lookupKey), String.format("LookupKey [%s] has not be found in available lookupKeys!", lookupKey));
            DataSourceLookupKeyHolder.setLookupKey(lookupKey);
            return joinPoint.proceed();
        } finally {
            DataSourceLookupKeyHolder.removeLookupKey();
        }
    }
}
