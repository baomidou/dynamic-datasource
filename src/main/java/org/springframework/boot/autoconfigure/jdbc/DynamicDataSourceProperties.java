package org.springframework.boot.autoconfigure.jdbc;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(prefix = "spring.datasource.dynamic")
public class DynamicDataSourceProperties {

  @NestedConfigurationProperty
  private DataSourceProperties master = new DataSourceProperties();

  private Map<String, DataSourceProperties> slaves = new HashMap<>();

}
