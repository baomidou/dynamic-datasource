/**
 * Copyright © 2018 organization baomidou
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

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.baomidou.dynamic.datasource.toolkit.DataSourceFactory.createDataSource;

/**
 * YML数据源提供者
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
@Slf4j
public class YmlDynamicDataSourceProvider implements DynamicDataSourceProvider {

    private DynamicDataSourceProperties properties;

    public YmlDynamicDataSourceProvider(DynamicDataSourceProperties properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, DataSource> loadDataSources() {
        Map<String, DataSourceProperty> dataSourcePropertiesMap = properties.getDatasource();
        Map<String, DataSource> dataSourceMap = new HashMap<>(dataSourcePropertiesMap.size());
        for (Map.Entry<String, DataSourceProperty> item : dataSourcePropertiesMap.entrySet()) {
            dataSourceMap.put(item.getKey(), createDataSource(item.getValue(), properties.getDruid()));
        }
        return dataSourceMap;
    }

}
