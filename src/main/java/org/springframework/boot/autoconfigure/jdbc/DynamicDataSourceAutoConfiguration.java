package org.springframework.boot.autoconfigure.jdbc;

import org.springframework.jdbc.datasource.DynamicDataSourceAspect;
import org.springframework.jdbc.datasource.DynamicDataSourceContextHolder;
import org.springframework.jdbc.datasource.DynamicRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvidersConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@Import({DataSourcePoolMetadataProvidersConfiguration.class, DataSourceInitializationConfiguration.class})
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
    DataSource masterDataSource = createDataSource(masterProperties);
    dataSourceMap.put("master", masterDataSource);
    DynamicDataSourceContextHolder.addDataSourceId("master");
    // add Slaves
    Map<String, DataSourceProperties> slavesProperties = properties.getSlaves();
    slavesProperties.forEach((k, v) -> {
      dataSourceMap.put(k, createDataSource(v));
      DynamicDataSourceContextHolder.addDataSourceId(k);
    });
    dynamicRoutingDataSource.setDefaultTargetDataSource(masterDataSource);
    dynamicRoutingDataSource.setTargetDataSources(dataSourceMap);
    return dynamicRoutingDataSource;
  }

  private DataSource createDataSource(DataSourceProperties properties) {
    if (properties.getType() == null) {
      properties.setType(HikariDataSource.class);
    }
    return properties.initializeDataSourceBuilder().build();
  }
}