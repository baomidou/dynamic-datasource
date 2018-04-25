package org.springframework.boot.autoconfigure.jdbc;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource.dynamic")
@Data
public class DynamicDataSourceProperties {

  private DataSourceProperties master;

  private List<DataSourceProperties> slaves;

}
