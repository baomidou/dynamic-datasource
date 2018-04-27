package org.springframework.jdbc.datasource;

public interface DynamicDataSourceStrategy {

  String determinSlaveDataSource(String[] slaveDataSourceLookupKeys);

}
