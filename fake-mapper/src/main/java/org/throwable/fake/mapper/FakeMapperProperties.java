package org.throwable.fake.mapper;

import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.ibatis.session.ExecutorType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author throwable
 * @version v1.0
 * @description
 * @since 2017/11/22 14:35
 */
@Data
@ConfigurationProperties(prefix = FakeMapperProperties.PREFIX)
public class FakeMapperProperties {

    public static final String PREFIX = "fake.mapper";
    public static final String DEFAULT_DATASOURCE_BEAN_NAME = "dataSource";
    public static final String DEFAULT_DATASOURCE_BEAN_CLASS_NAME = "javax.sql.DataSource";

    private String dataSourceBeanName = DEFAULT_DATASOURCE_BEAN_NAME;
    private String dataSourceBeanClassName = DEFAULT_DATASOURCE_BEAN_CLASS_NAME;
    private String configurationLocation;
    private String mapperPackages;
    private String[] mapperLocations;
    private String typeAliasesPackage;
    private String typeHandlersPackage;
    private Boolean checkConfigurationLocation = false;
    private Boolean enablePaginationPlugin = false;
    private Boolean enableSqlStatisticsPlugin = false;
    private Properties configurationProperties;
    private ExecutorType executorType;

    public Resource[] resolveMapperLocations() {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<Resource> resources = Lists.newArrayList();
        if (null != this.mapperLocations) {
            for (String mapperLocation : this.mapperLocations) {
                try {
                    Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }
}
