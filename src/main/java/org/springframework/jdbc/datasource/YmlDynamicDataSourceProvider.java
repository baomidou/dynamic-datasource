package org.springframework.jdbc.datasource;

import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DynamicDataSourceProperties;

public class YmlDynamicDataSourceProvider implements DynamicDataSourceProvider {

  private DynamicDataSourceProperties properties;

  public YmlDynamicDataSourceProvider(DynamicDataSourceProperties properties) {
    this.properties = properties;
  }

  @Override
  public DataSource loadMasterDataSource() {
    return createDataSource(properties.getMaster());
  }

  @Override
  public Map<String,DataSource> loadSlaveDataSource() {
    Map<String,DataSource> dataSourceMap = new HashMap<>(2);
    Map<String, DataSourceProperties> slave = properties.getSlave();
    slave.forEach((k, v) -> {
      dataSourceMap.put(k, createDataSource(v));
    });
    return dataSourceMap;
  }

  private DataSource createDataSource(DataSourceProperties properties) {
    if (properties.getType() == null) {
      properties.setType(HikariDataSource.class);
    }
    return properties.initializeDataSourceBuilder().build();
  }

}
