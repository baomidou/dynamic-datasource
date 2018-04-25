package org.springframework.boot.autoconfigure.jdbc;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceAutoConfiguration implements ImportBeanDefinitionRegistrar, EnvironmentAware {

  private final DynamicDataSourceProperties properties;

  public DynamicDataSourceAutoConfiguration(DynamicDataSourceProperties properties) {
    this.properties = properties;
  }

  @Bean(name = "master")
  @Primary
  public DataSource primaryDataSource() {
    DataSourceProperties master = properties.getMaster();
    return DataSourceBuilder.create().build();
  }

  @Bean(name = "secondaryDataSource")
  @ConfigurationProperties(prefix = "spring.datasource.secondary")
  public DataSource secondaryDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean("dynamicDataSource")
  public DataSource dynamicDataSource() {
    DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();

    Map<Object, Object> dataSourceMap = new HashMap<>(4);
    dataSourceMap.put("master", master);
    dataSourceMap.put("slave", slave);
    dynamicRoutingDataSource.setTargetDataSources(dataSourceMap);

    dynamicRoutingDataSource.setDefaultTargetDataSource(master);

    DynamicDataSourceContextHolder.addDataSourceId("master", "slave");

    return dynamicRoutingDataSource;
  }

//  @Bean
//  @ConditionalOnClass
//  public SqlSessionFactory sqlSessionFactory(@Qualifier("dynamicDataSource") DataSource dataSource)
//      throws Exception {
//    SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
//    Resource[] mapperLocations = new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*Mapper.xml");
//    sqlSessionFactoryBean.setMapperLocations(mapperLocations);
//    sqlSessionFactoryBean.setDataSource(dataSource);
//    return sqlSessionFactoryBean.getObject();
//  }

  @Bean
  public PlatformTransactionManager transactionManager(@Qualifier("dynamicDataSource") DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Override
  public void setEnvironment(Environment environment) {

  }

  public DataSource buildDataSource(Map<String, DataSourceProperties> dsMap) {
    Object type = dsMap.get("type");
    Class<? extends DataSource> dataSourceType;
    try {
      dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);
      String driverClassName = dsMap.get("driverClassName").toString();
      String url = dsMap.get("url").toString();
      String username = dsMap.get("username").toString();
      String password = dsMap.get("password").toString();
      DataSourceBuilder factory = DataSourceBuilder.create().driverClassName(driverClassName).url(url)
          .username(username).password(password).type(dataSourceType);
      return factory.build();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return null;

  }

  @Override
  public void registerBeanDefinitions(AnnotationMetadata annotationMetadata,
      BeanDefinitionRegistry beanDefinitionRegistry) {

  }
}