package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.metrics.micrometer.MicrometerMetricsTrackerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceUnwrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

import javax.sql.DataSource;

/**
 *
 * @author qiuyouyao
 * @date 2025-07-30 08:50:34
 *
 */
@AutoConfiguration(after = {
        MetricsAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SimpleMetricsExportAutoConfiguration.class,
        DynamicDataSourceAutoConfiguration.class
})
@ConditionalOnClass({DataSource.class, MeterRegistry.class})
@ConditionalOnBean({DataSource.class, MeterRegistry.class})
@Slf4j
public class DynamicDataSourcePoolMetricsAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(HikariDataSource.class)
    @ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "hikari-metrics.enabled", havingValue = "true", matchIfMissing = true)
    static class HikariDataSourceMetricsConfiguration {

        @Bean
        HikariDataSourceMeterBinder dynamicHikariDataSourceMeterBinder(ObjectProvider<DataSource> dataSources) {
            return new HikariDataSourceMeterBinder(dataSources);
        }

        static class HikariDataSourceMeterBinder implements MeterBinder {

            private final ObjectProvider<DataSource> dataSources;

            HikariDataSourceMeterBinder(ObjectProvider<DataSource> dataSources) {
                this.dataSources = dataSources;
            }

            @Override
            public void bindTo(@NonNull MeterRegistry registry) {
                DataSource dynamicdataSource = dataSources.getIfAvailable();
                if (!(dynamicdataSource instanceof DynamicRoutingDataSource)) {
                    return;
                }
                DynamicRoutingDataSource dynamicRoutingDataSource = (DynamicRoutingDataSource) dynamicdataSource;
                for (DataSource dataSource : dynamicRoutingDataSource.getDataSources().values()) {
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
    }
}
