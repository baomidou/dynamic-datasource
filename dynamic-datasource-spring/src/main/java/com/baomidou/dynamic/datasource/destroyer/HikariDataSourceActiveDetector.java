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
