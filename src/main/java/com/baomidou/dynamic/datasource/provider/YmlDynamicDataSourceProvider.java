/*
 * Copyright © 2018 organization baomidou
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
package com.baomidou.dynamic.datasource.provider;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.Map;

/**
 * YML数据源提供者
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
@Slf4j
@AllArgsConstructor
public class YmlDynamicDataSourceProvider extends AbstractDataSourceProvider {

    /**
     * 所有数据源
     */
    private final Map<String, DataSourceProperty> dataSourcePropertiesMap;

    @Override
    public Map<String, DataSource> loadDataSources() {
        return createDataSourceMap(dataSourcePropertiesMap);
    }
}
