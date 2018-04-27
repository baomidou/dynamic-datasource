package org.springframework.jdbc.datasource;

import java.util.concurrent.ThreadLocalRandom;

public class RandomDynamicDataSourceStrategy implements DynamicDataSourceStrategy {

  @Override
  public String determinSlaveDataSource(String[] slaveDataSourceLookupKeys) {
    int i = ThreadLocalRandom.current().nextInt(slaveDataSourceLookupKeys.length);
    return slaveDataSourceLookupKeys[i];
  }

}
