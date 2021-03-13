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

import javax.sql.DataSource;
import java.util.Map;

/**
 * 多数据源加载接口，默认的实现为从yml信息中加载所有数据源 你可以自己实现从其他地方加载所有数据源
 *
 * @author TaoYu Kanyuxia
 * @see YmlDynamicDataSourceProvider
 * @see AbstractJdbcDataSourceProvider
 * @since 1.0.0
 */
public interface DynamicDataSourceProvider {

    /**
     * 加载所有数据源
     *
     * @return 所有数据源，key为数据源名称
     */
    Map<String, DataSource> loadDataSources();
}
