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
 * Hikari data source pool active detector.
 *
 * @author alvinkwok
 * @since 2023/10/18
 */
public class HikariDataSourceActiveDetector implements DataSourceActiveDetector {
    @Override
    @SneakyThrows(ReflectiveOperationException.class)
    public boolean containsActiveConnection(DataSource dataSource) {
        Object hikariPoolMXBean = dataSource.getClass().getMethod("getHikariPoolMXBean").invoke(dataSource);
        int activeCount = null == hikariPoolMXBean
                ? 0
                : (int) hikariPoolMXBean.getClass().getMethod("getActiveConnections").invoke(hikariPoolMXBean);
        return activeCount != 0;
    }

    @Override
    public boolean support(DataSource dataSource) {
        return "com.zaxxer.hikari.HikariDataSource".equals(dataSource.getClass().getName());
    }
}
