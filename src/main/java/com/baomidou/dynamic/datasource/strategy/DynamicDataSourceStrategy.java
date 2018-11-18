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
package com.baomidou.dynamic.datasource.strategy;

import javax.sql.DataSource;
import java.util.List;

/**
 * 多数据源选择策略接口
 * 一般默认为负载均衡策略，默认提供了一个随机策略
 *
 * @author TaoYu Kanyuxia
 * @see RandomDynamicDataSourceStrategy
 * @see LoadBalanceDynamicDataSourceStrategy
 * @since 1.0.0
 */
public interface DynamicDataSourceStrategy {

    /**
     * 决定当前数据源
     *
     * @param dataSources 数据源选择库
     * @return dataSource 所选择的数据源
     */
    DataSource determineDataSource(List<DataSource> dataSources);
}
