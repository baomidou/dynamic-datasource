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
package com.baomidou.dynamic.datasource.destroyer;

import lombok.SneakyThrows;

import javax.sql.DataSource;

/**
 * Description
 * alibaba Druid data source pool active detector.
 *
 * @author alvinkwok
 * @since 2023/10/18
 */
public class DruidDataSourceActiveDetector implements DataSourceActiveDetector {
    @Override
    @SneakyThrows(ReflectiveOperationException.class)
    public boolean containsActiveConnection(DataSource dataSource) {
        int activeCount = (int) dataSource.getClass().getMethod("getActiveCount").invoke(dataSource);
        return activeCount != 0;
    }

    @Override
    public boolean support(DataSource dataSource) {
        return "com.alibaba.druid.pool.DruidDataSource".equals(dataSource.getClass().getName());
    }
}
