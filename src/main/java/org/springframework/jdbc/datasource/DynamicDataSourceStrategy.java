package org.springframework.jdbc.datasource;

import java.util.Set;

public interface DynamicDataSourceStrategy {

  String determinSlaveDataSource(String[] slaveDataSourceLookupKeys);

}
