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

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@AutoConfiguration(after = {
        MetricsAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SimpleMetricsExportAutoConfiguration.class,
        DynamicDataSourceAutoConfiguration.class
})
@ConditionalOnClass({DataSource.class, MeterRegistry.class})
@ConditionalOnBean({DataSource.class, MeterRegistry.class})
public class DynamicDataSourcePoolMetricsBoot4AutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(com.zaxxer.hikari.HikariDataSource.class)
    @ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "hikari-metrics.enabled", havingValue = "true", matchIfMissing = true)
    static class HikariDataSourceMetricsConfiguration {

        @Bean
        DynamicDataSourceHikariMeterBinder dynamicHikariDataSourceMeterBinder(ObjectProvider<DataSource> dataSources) {
            return new DynamicDataSourceHikariMeterBinder(dataSources);
        }
    }
}
