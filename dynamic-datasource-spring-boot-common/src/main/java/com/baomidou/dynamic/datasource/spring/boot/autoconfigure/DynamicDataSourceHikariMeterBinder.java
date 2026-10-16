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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.micrometer.MicrometerMetricsTrackerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.jdbc.DataSourceUnwrapper;

import javax.sql.DataSource;

@Slf4j
final class DynamicDataSourceHikariMeterBinder implements MeterBinder {

    private final ObjectProvider<DataSource> dataSources;

    DynamicDataSourceHikariMeterBinder(ObjectProvider<DataSource> dataSources) {
        this.dataSources = dataSources;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        DataSource dynamicDataSource = dataSources.getIfAvailable();
        if (!(dynamicDataSource instanceof DynamicRoutingDataSource)) {
            return;
        }
        DynamicRoutingDataSource routingDataSource = (DynamicRoutingDataSource) dynamicDataSource;
        for (DataSource dataSource : routingDataSource.getDataSources().values()) {
            HikariDataSource hikariDataSource = DataSourceUnwrapper.unwrap(dataSource, HikariConfigMXBean.class, HikariDataSource.class);
            if (hikariDataSource != null) {
                bindMetricsRegistryToHikariDataSource(hikariDataSource, registry);
            }
        }
    }

    private void bindMetricsRegistryToHikariDataSource(HikariDataSource hikari, MeterRegistry registry) {
        if (hikari.getMetricRegistry() == null && hikari.getMetricsTrackerFactory() == null) {
            try {
                hikari.setMetricsTrackerFactory(new MicrometerMetricsTrackerFactory(registry));
            } catch (Exception ex) {
                log.warn("Failed to bind Hikari metrics: {}", ex.getMessage());
            }
        }
    }
}
