package org.throwable.fake.mapper.support.plugins.generator.identity;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/8/17 19:57
 */
public abstract class PrimaryKeyGeneratorRepository {

    private static final Map<Class<?>, PrimaryKeyGenerator> generators = Maps.newHashMap();

    public static boolean registerKeyGenerator(Class<?> entityClass, Class<? extends PrimaryKeyGenerator> keyGenerator) {
        try {
            Class<?> clazz = Class.forName(keyGenerator.getName());
            return null != generators.putIfAbsent(entityClass, (PrimaryKeyGenerator) clazz.newInstance());
        } catch (Exception e) {
            //ignore
        }
        return false;
    }

    public static boolean registerKeyGenerator(Class<?> entityClass, PrimaryKeyGenerator keyGenerator) {
        return null != generators.putIfAbsent(entityClass, keyGenerator);
    }

    public static PrimaryKeyGenerator getKeyGeneratorByEntityClass(Class<?> entityClass) {
        return generators.get(entityClass);
    }


}
