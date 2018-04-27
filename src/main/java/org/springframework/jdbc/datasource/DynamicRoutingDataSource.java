package org.springframework.jdbc.datasource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

  private String[] slaveDataSourceLookupKeys;

  @Setter
  private DynamicDataSourceProvider dynamicDataSourceProvider;
  @Setter
  private DynamicDataSourceStrategy dynamicDataSourceStrategy;

  @Override
  protected Object determineCurrentLookupKey() {
    String dataSourceLookupKey = DynamicDataSourceContextHolder.getDataSourceLookupKey();
    if (dataSourceLookupKey != null && "".equals(dataSourceLookupKey)) {
      String targetDataSourceLookupKey = dynamicDataSourceStrategy.determinSlaveDataSource(slaveDataSourceLookupKeys);
      log.info("dataSourceLookupKey is empty,now switch to slave of name : {}", targetDataSourceLookupKey);
      return targetDataSourceLookupKey;
    }
    return dataSourceLookupKey;
  }

  @Override
  public void afterPropertiesSet() {
    DataSource masterDataSource = dynamicDataSourceProvider.loadMasterDataSource();
    Map<String, DataSource> slaveDataSource = dynamicDataSourceProvider.loadSlaveDataSource();

    Set<String> slaveDataSourceIds = slaveDataSource.keySet();
    this.slaveDataSourceLookupKeys = slaveDataSourceIds.toArray(new String[slaveDataSource.size()]);

    Map<Object, Object> targetDataSource = new HashMap<>();
    targetDataSource.put("master", masterDataSource);
    targetDataSource.putAll(slaveDataSource);
    super.setDefaultTargetDataSource(masterDataSource);
    super.setTargetDataSources(targetDataSource);

    super.afterPropertiesSet();
  }

}