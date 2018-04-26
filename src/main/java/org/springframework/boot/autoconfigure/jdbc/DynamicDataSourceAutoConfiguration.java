package org.springframework.boot.autoconfigure.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceAutoConfiguration {

  private final DynamicDataSourceProperties properties;

  public DynamicDataSourceAutoConfiguration(DynamicDataSourceProperties properties) {
    this.properties = properties;
  }

  @Bean
  public DynamicDataSourceAspect dynamicDataSourceAspect() {
    return new DynamicDataSourceAspect();
  }

  @Bean
  public DataSource dynamicDataSource() {
    DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
    Map<Object, Object> dataSourceMap = new HashMap<>();
    // add Master
    DataSourceProperties masterProperties = properties.getMaster();
    dataSourceMap.put("master", createDataSource(masterProperties, HikariDataSource.class));
    DynamicDataSourceContextHolder.addDataSourceId("master");
    // add Slaves
    Map<String, DataSourceProperties> slavesProperties = properties.getSlaves();
    slavesProperties.forEach((k, v) -> {
      dataSourceMap.put(k, createDataSource(v, HikariDataSource.class));
      DynamicDataSourceContextHolder.addDataSourceId(k);
    });
    dynamicRoutingDataSource.setTargetDataSources(dataSourceMap);
    return dynamicRoutingDataSource;
  }

  private DataSource createDataSource(DataSourceProperties properties,
      Class<? extends DataSource> type) {
    return properties.initializeDataSourceBuilder().type(type).build();
  }
}