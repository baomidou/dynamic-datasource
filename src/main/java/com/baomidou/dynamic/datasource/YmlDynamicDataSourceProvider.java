/**
 * Copyright © 2018 TaoYu (tracy5546@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baomidou.dynamic.datasource;

import com.baomidou.dynamic.datasource.spring.boot.DynamicDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

/**
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
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
  public Map<String, DataSource> loadSlaveDataSource() {
    Map<String, DataSource> dataSourceMap = new HashMap<>(2);
    Map<String, DataSourceProperties> slave = properties.getSlave();
    slave.forEach((k, v) -> dataSourceMap.put(k, createDataSource(v)));
    return dataSourceMap;
  }

  private DataSource createDataSource(DataSourceProperties properties) {
    if (properties.getType() == null) {
      properties.setType(HikariDataSource.class);
    }
    return properties.initializeDataSourceBuilder().build();
  }

}
