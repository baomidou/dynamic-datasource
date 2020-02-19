/**
 * Copyright © 2019 organization humingfeng
 * <pre>
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
 * <pre/>
 */
package com.baomidou.dynamic.datasource.provider;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.Map;

/**
 * YML数据源提供者
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
@Slf4j
public class YmlDynamicDataSourceProvider extends AbstractDataSourceProvider implements DynamicDataSourceProvider {

    /**
    * 所有数据源
    */
    private Map<String, DataSourceProperty> dataSourcePropertiesMap;

    /**
     * 多数据源参数
     */
    @Autowired
    private DynamicDataSourceProperties properties;

    /**
     * 数据库配置表初始化数据源
     */
    private MysqlDynamicDataSourceProvider mysqlDynamicDataSourceProvider = new MysqlDynamicDataSourceProvider();

    public YmlDynamicDataSourceProvider(Map<String, DataSourceProperty> dataSourcePropertiesMap) {
        this.dataSourcePropertiesMap = dataSourcePropertiesMap;
    }

    @Override
    public Map<String, DataSource> loadDataSources() {

      Map<String, DataSource> dataSourceMap = createDataSourceMap(dataSourcePropertiesMap);

      /**
       *    在此处获取其他数据库配置的数据源
       */
      Map<String, DataSourceProperty> otherMap = mysqlDynamicDataSourceProvider.run(dataSourceMap.get(properties.getPrimary()));

      Map<String, DataSource> otherDataSourceMap = createDataSourceMap(otherMap);

      dataSourceMap.putAll(otherDataSourceMap);

      return dataSourceMap;
    }
}
