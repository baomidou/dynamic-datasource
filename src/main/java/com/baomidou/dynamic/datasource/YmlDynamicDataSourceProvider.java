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
import com.baomidou.dynamic.datasource.spring.boot.DynamicItemDataSourceProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
@Slf4j
public class YmlDynamicDataSourceProvider extends AbstractDynamicDataSourceProvider implements DynamicDataSourceProvider {

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
    Map<String, DynamicItemDataSourceProperties> slaves = properties.getSlave();
    if (slaves.size() == 0) {
      throw new RuntimeException("dynamic datasource ** slave ** is empty, please check your config");
    }
    Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(slaves.size());
    for (Map.Entry<String, DynamicItemDataSourceProperties> item : slaves.entrySet()) {
      dataSourceMap.put(item.getKey(), createDataSource(item.getValue()));
    }
    return dataSourceMap;
  }

}
