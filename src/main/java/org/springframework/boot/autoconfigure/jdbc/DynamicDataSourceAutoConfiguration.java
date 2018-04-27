package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DynamicDataSourceAspect;
import org.springframework.jdbc.datasource.DynamicDataSourceProvider;
import org.springframework.jdbc.datasource.DynamicDataSourceStrategy;
import org.springframework.jdbc.datasource.DynamicRoutingDataSource;
import org.springframework.jdbc.datasource.LoadBalancDynamicDataSourceStrategy;
import org.springframework.jdbc.datasource.YmlDynamicDataSourceProvider;

@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class DynamicDataSourceAutoConfiguration {

  private final DynamicDataSourceProperties properties;

  public DynamicDataSourceAutoConfiguration(DynamicDataSourceProperties properties) {
    this.properties = properties;
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamicDataSourceStrategy dynamicDataSourceStrategy() {
    return new LoadBalancDynamicDataSourceStrategy();
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamicDataSourceProvider dynamicDataSourceProvider() {
    return new YmlDynamicDataSourceProvider(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamicRoutingDataSource dynamicDataSource(DynamicDataSourceProvider dynamicDataSourceProvider,
      DynamicDataSourceStrategy dynamicDataSourceStrategy) {
    DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
    dynamicRoutingDataSource.setDynamicDataSourceProvider(dynamicDataSourceProvider);
    dynamicRoutingDataSource.setDynamicDataSourceStrategy(dynamicDataSourceStrategy);
    return dynamicRoutingDataSource;
  }

  @Bean
  @ConditionalOnMissingBean
  public DynamicDataSourceAspect dynamicDataSourceAspect(DynamicRoutingDataSource dynamicRoutingDataSource) {
    return new DynamicDataSourceAspect(dynamicRoutingDataSource);
  }

}