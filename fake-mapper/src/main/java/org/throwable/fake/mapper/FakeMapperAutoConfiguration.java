package org.throwable.fake.mapper;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.*;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.throwable.fake.mapper.configuration.FakeMapperRegistrar;
import org.throwable.fake.mapper.support.plugins.pagination.PaginationPlugin;
import org.throwable.fake.mapper.support.plugins.statistics.SqlStatisticsPlugin;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/22 14:34
 */
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class, DataSourceAutoConfiguration.class})
@EnableConfigurationProperties(value = FakeMapperProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@Configuration
@Slf4j
public class FakeMapperAutoConfiguration implements ResourceLoaderAware, BeanFactoryAware, SmartInitializingSingleton {

    private static final String SQL_SESSION_FACTORY_BEAN_NAME = "sqlSessionFactory";
    private static final String MAPPER_REGISTRAR = "fakeMapperRegistrar";
    private static final String SEPARATOR = ",";
    private ResourceLoader resourceLoader;
    private DefaultListableBeanFactory beanFactory;
    private final FakeMapperProperties fakeMapperProperties;
    private FakeMapperRegistrar fakeMapperRegistrar;

    public FakeMapperAutoConfiguration(FakeMapperProperties fakeMapperProperties) {
        this.fakeMapperProperties = fakeMapperProperties;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        checkConfigurationFileExists();
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setVfs(SpringBootVfs.class);
        factory.setDataSource(attemptToFetchDataSourceBean());
        if (StringUtils.hasText(this.fakeMapperProperties.getConfigurationLocation())) {
            factory.setConfigLocation(this.resourceLoader.getResource(this.fakeMapperProperties.getConfigurationLocation()));
        }
        if (null != this.fakeMapperProperties.getConfigurationProperties()) {
            factory.setConfigurationProperties(this.fakeMapperProperties.getConfigurationProperties());
        }
        Interceptor[] interceptors = processInterceptors();
        if (!ObjectUtils.isEmpty(interceptors)) {
            factory.setPlugins(interceptors);
        }
        DatabaseIdProvider databaseIdProvider = fetchDatabaseIdProviderBean();
        if (null != databaseIdProvider) {
            factory.setDatabaseIdProvider(databaseIdProvider);
        }
        if (StringUtils.hasLength(this.fakeMapperProperties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(this.fakeMapperProperties.getTypeAliasesPackage());
        }
        if (StringUtils.hasLength(this.fakeMapperProperties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.fakeMapperProperties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(this.fakeMapperProperties.resolveMapperLocations())) {
            factory.setMapperLocations(this.fakeMapperProperties.resolveMapperLocations());
        }
        factory.setTypeHandlers(processJSR310TypeHandlers());
        return factory.getObject();
    }

    private void checkConfigurationFileExists() {
        if (this.fakeMapperProperties.getCheckConfigurationLocation()
                && StringUtils.hasText(this.fakeMapperProperties.getConfigurationLocation())) {
            Resource resource = this.resourceLoader.getResource(this.fakeMapperProperties.getConfigurationLocation());
            Assert.state(resource.exists(), "Cannot find config location: " + resource
                    + " (please add config file or check your Mybatis configuration)");
        }
    }

    private DataSource attemptToFetchDataSourceBean() {
        Object target;
        try {
            target = beanFactory.getBean(fakeMapperProperties.getDataSourceBeanName());
            if (null != target && DataSource.class.isAssignableFrom(target.getClass())) {
                return (DataSource) target;
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Attempt to fetch dataSource bean by beanName [{}] failed", fakeMapperProperties.getDataSourceBeanName(), e);
            }
            //ignore
        }
        try {
            target = beanFactory.getBean(Class.forName(fakeMapperProperties.getDataSourceBeanClassName()));
            if (null != target && DataSource.class.isAssignableFrom(target.getClass())) {
                return (DataSource) target;
            }
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Attempt to fetch dataSource bean by bean className [{}] failed", fakeMapperProperties.getDataSourceBeanClassName(), e);
            }
            //ignore
        }
        throw new IllegalArgumentException(String.format("Fetch datasource bean failed for beanName [%s] or beanClassName [%s]",
                fakeMapperProperties.getDataSourceBeanName(), fakeMapperProperties.getDataSourceBeanClassName()));
    }

    private Interceptor[] processInterceptors() {
        List<Interceptor> interceptorList = Lists.newArrayList();
        if (fakeMapperProperties.getEnablePaginationPlugin()) {
            interceptorList.add(new PaginationPlugin());
        }
        if (fakeMapperProperties.getEnableSqlStatisticsPlugin()) {
            interceptorList.add(new SqlStatisticsPlugin());
        }
        try {
            Map<String, Interceptor> interceptors = beanFactory.getBeansOfType(Interceptor.class);
            if (null != interceptors && !interceptors.isEmpty()) {
                interceptorList.addAll(interceptors.values());
            }
        } catch (Exception e) {
            //ignore
        }
        return interceptorList.toArray(new Interceptor[interceptorList.size()]);
    }

    private DatabaseIdProvider fetchDatabaseIdProviderBean() {
        try {
            return beanFactory.getBean(DatabaseIdProvider.class);
        } catch (Exception e) {
            //ignore
        }
        return null;
    }

    private TypeHandler<?>[] processJSR310TypeHandlers() {
        List<TypeHandler<?>> typeHandlers = new ArrayList<>();
        typeHandlers.add(new InstantTypeHandler());
        typeHandlers.add(new LocalDateTimeTypeHandler());
        typeHandlers.add(new LocalDateTypeHandler());
        typeHandlers.add(new LocalTimeTypeHandler());
        typeHandlers.add(new OffsetDateTimeTypeHandler());
        typeHandlers.add(new OffsetTimeTypeHandler());
        typeHandlers.add(new ZonedDateTimeTypeHandler());
        return typeHandlers.toArray(new TypeHandler[typeHandlers.size()]);
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        ExecutorType executorType = this.fakeMapperProperties.getExecutorType();
        if (null != executorType) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

    public static class AutoConfiguredMapperScannerRegistrar
            implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

        private BeanFactory beanFactory;

        private ResourceLoader resourceLoader;

        private Environment environment;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {
            processScanningMybatisMapperBeans(registry, resourceLoader, environment);
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }

        @Override
        public void setEnvironment(Environment environment) {
            this.environment = environment;
        }
    }

    private static void processScanningMybatisMapperBeans(BeanDefinitionRegistry registry,
                                                          ResourceLoader resourceLoader,
                                                          Environment environment) {
        String mapperPackagesProperty = environment.getProperty(String.format("%s.%s", FakeMapperProperties.PREFIX, "mapper-packages"));
        if (!StringUtils.hasText(mapperPackagesProperty)) {
            mapperPackagesProperty = environment.getProperty(String.format("%s.%s", FakeMapperProperties.PREFIX, "mapperPackages"));
        }
        Assert.isTrue(StringUtils.hasText(mapperPackagesProperty), "Configuration property [mapper-packages] must not be empty!");
        if (log.isDebugEnabled()) {
            log.debug("Scanning mapper interfaces and register them into ioc container");
        }
        ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);
        if (null != resourceLoader) {
            scanner.setResourceLoader(resourceLoader);
        }
        scanner.setAddToConfig(true);
        scanner.setSqlSessionFactoryBeanName(SQL_SESSION_FACTORY_BEAN_NAME);
        List<String> mapperPackages = Lists.newArrayList();
        for (String mapperPackage : mapperPackagesProperty.split(SEPARATOR)) {
            if (StringUtils.hasText(mapperPackage)) {
                mapperPackages.add(mapperPackage);
            }
        }
        Assert.notEmpty(mapperPackages, "All elements of Configuration property [basePackages] are blank!");
        if (log.isDebugEnabled()) {
            mapperPackages.forEach(pkg -> log.debug("Using scanner to scan package '{}'", pkg));
        }
        scanner.setAnnotationClass(Mapper.class);
        scanner.registerFilters();
        scanner.doScan(StringUtils.toStringArray(mapperPackages));
    }

    @Configuration
    @Import({AutoConfiguredMapperScannerRegistrar.class})
    @ConditionalOnMissingBean(MapperFactoryBean.class)
    public static class MapperScannerRegistrarNotFoundConfiguration {

        @PostConstruct
        public void afterPropertiesSet() {
            log.debug("No {} found.", MapperFactoryBean.class.getName());
        }
    }

    @Override
    public void afterSingletonsInstantiated() {
        registerMapperRegistrar();
        registerFakeMappers();
    }

    private void registerMapperRegistrar() {
        SqlSessionFactory sqlSessionFactory = beanFactory.getBean(SqlSessionFactory.class);
        String mapperPackages = fakeMapperProperties.getMapperPackages();
        Assert.isTrue(StringUtils.hasText(mapperPackages), "Configuration property [mapper-packages] must not be empty!");
        String[] mapperPackagesArray = mapperPackages.split(SEPARATOR);
        FakeMapperRegistrar fakeMapperRegistrar = new FakeMapperRegistrar(sqlSessionFactory.getConfiguration(), mapperPackagesArray);
        beanFactory.registerSingleton(MAPPER_REGISTRAR, fakeMapperRegistrar);
        this.fakeMapperRegistrar = beanFactory.getBean(MAPPER_REGISTRAR, FakeMapperRegistrar.class);
        Assert.notNull(this.fakeMapperRegistrar, "Fetch FakeMapperRegistrar bean from spring context failed!");
    }

    private void registerFakeMappers() {
        this.fakeMapperRegistrar.registerMappers();
    }
}
