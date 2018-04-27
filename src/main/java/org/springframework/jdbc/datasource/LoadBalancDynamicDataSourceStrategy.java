package org.springframework.jdbc.datasource;

import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancDynamicDataSourceStrategy implements DynamicDataSourceStrategy {

  private AtomicInteger count = new AtomicInteger(0);

  @Override
  public String determinSlaveDataSource(String[] slaveDataSourceLookupKeys) {
    int number = count.getAndAdd(1);
    return slaveDataSourceLookupKeys[number % slaveDataSourceLookupKeys.length];
  }

}
