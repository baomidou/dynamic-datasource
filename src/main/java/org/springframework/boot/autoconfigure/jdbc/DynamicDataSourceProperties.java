package org.springframework.boot.autoconfigure.jdbc;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.datasource.dynamic")
public class DynamicDataSourceProperties {

  private DataSourceProperties master = new DataSourceProperties() {
  };

  private Map<String, DataSourceProperties> slave = new HashMap<>();

}
