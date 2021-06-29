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
package com.baomidou.dynamic.datasource.creator;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.baomidou.dynamic.datasource.exception.ErrorCreateDataSourceException;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidSlf4jConfig;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidWallConfigUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.baomidou.dynamic.datasource.support.DdConstants.DRUID_DATASOURCE;

/**
 * Druid数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/21
 */
public class DruidDataSourceCreator extends AbstractDataSourceCreator implements DataSourceCreator {

    private static Boolean druidExists = false;

    static {
        try {
            Class.forName(DRUID_DATASOURCE);
            druidExists = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private final DruidConfig gConfig;

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    public DruidDataSourceCreator(DynamicDataSourceProperties dynamicDataSourceProperties) {
        super(dynamicDataSourceProperties);
        this.gConfig = dynamicDataSourceProperties.getDruid();
    }

    @Override
    public DataSource doCreateDataSource(DataSourceProperty dataSourceProperty) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setUrl(dataSourceProperty.getUrl());
        dataSource.setName(dataSourceProperty.getPoolName());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (!StringUtils.isEmpty(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }
        DruidConfig config = dataSourceProperty.getDruid();
        Properties properties = config.toProperties(gConfig);

        List<Filter> proxyFilters = this.initFilters(dataSourceProperty, properties);
        dataSource.setProxyFilters(proxyFilters);

        dataSource.configFromPropety(properties);
        //连接参数单独设置
        dataSource.setConnectProperties(config.getConnectionProperties());
        //设置druid内置properties不支持的的参数
        this.setParam(dataSource, config);

        if (Boolean.FALSE.equals(dataSourceProperty.getLazy())) {
            try {
                dataSource.init();
            } catch (SQLException e) {
                throw new ErrorCreateDataSourceException("druid create error", e);
            }
        }
        return dataSource;
    }

    private List<Filter> initFilters(DataSourceProperty dataSourceProperty, Properties properties) {
        List<Filter> proxyFilters = new ArrayList<>(2);
        String filters = properties.getProperty("druid.filters");
        if (!StringUtils.isEmpty(filters)) {
            if (filters.contains("stat")) {
                StatFilter statFilter = new StatFilter();
                statFilter.configFromProperties(properties);
                proxyFilters.add(statFilter);
            }
            if (filters.contains("wall")) {
                WallConfig wallConfig = DruidWallConfigUtil.toWallConfig(dataSourceProperty.getDruid().getWall(), gConfig.getWall());
                WallFilter wallFilter = new WallFilter();
                wallFilter.setConfig(wallConfig);
                proxyFilters.add(wallFilter);
            }
            if (filters.contains("slf4j")) {
                Slf4jLogFilter slf4jLogFilter = new Slf4jLogFilter();
                // 由于properties上面被用了，LogFilter不能使用configFromProperties方法，这里只能一个个set了。
                DruidSlf4jConfig slf4jConfig = gConfig.getSlf4j();
                slf4jLogFilter.setStatementLogEnabled(slf4jConfig.getEnable());
                slf4jLogFilter.setStatementExecutableSqlLogEnable(slf4jConfig.getStatementExecutableSqlLogEnable());
                proxyFilters.add(slf4jLogFilter);
            }
        }
        if (this.applicationContext != null) {
            for (String filterId : gConfig.getProxyFilters()) {
                proxyFilters.add(this.applicationContext.getBean(filterId, Filter.class));
            }
        }
        return proxyFilters;
    }

    private void setParam(DruidDataSource dataSource, DruidConfig config) {
        String defaultCatalog = config.getDefaultCatalog() == null ? gConfig.getDefaultCatalog() : config.getDefaultCatalog();
        if (defaultCatalog != null) {
            dataSource.setDefaultCatalog(defaultCatalog);
        }
        Boolean defaultAutoCommit = config.getDefaultAutoCommit() == null ? gConfig.getDefaultAutoCommit() : config.getDefaultAutoCommit();
        if (defaultAutoCommit != null && !defaultAutoCommit) {
            dataSource.setDefaultAutoCommit(false);
        }
        Boolean defaultReadOnly = config.getDefaultReadOnly() == null ? gConfig.getDefaultReadOnly() : config.getDefaultReadOnly();
        if (defaultReadOnly != null) {
            dataSource.setDefaultReadOnly(defaultReadOnly);
        }
        Integer defaultTransactionIsolation = config.getDefaultTransactionIsolation() == null ? gConfig.getDefaultTransactionIsolation() : config.getDefaultTransactionIsolation();
        if (defaultTransactionIsolation != null) {
            dataSource.setDefaultTransactionIsolation(defaultTransactionIsolation);
        }

        Boolean testOnReturn = config.getTestOnReturn() == null ? gConfig.getTestOnReturn() : config.getTestOnReturn();
        if (testOnReturn != null && testOnReturn) {
            dataSource.setTestOnReturn(true);
        }
        Integer validationQueryTimeout =
                config.getValidationQueryTimeout() == null ? gConfig.getValidationQueryTimeout() : config.getValidationQueryTimeout();
        if (validationQueryTimeout != null && !validationQueryTimeout.equals(-1)) {
            dataSource.setValidationQueryTimeout(validationQueryTimeout);
        }

        Boolean sharePreparedStatements =
                config.getSharePreparedStatements() == null ? gConfig.getSharePreparedStatements() : config.getSharePreparedStatements();
        if (sharePreparedStatements != null && sharePreparedStatements) {
            dataSource.setSharePreparedStatements(true);
        }
        Integer connectionErrorRetryAttempts =
                config.getConnectionErrorRetryAttempts() == null ? gConfig.getConnectionErrorRetryAttempts()
                        : config.getConnectionErrorRetryAttempts();
        if (connectionErrorRetryAttempts != null && !connectionErrorRetryAttempts.equals(1)) {
            dataSource.setConnectionErrorRetryAttempts(connectionErrorRetryAttempts);
        }
        Boolean breakAfterAcquireFailure =
                config.getBreakAfterAcquireFailure() == null ? gConfig.getBreakAfterAcquireFailure() : config.getBreakAfterAcquireFailure();
        if (breakAfterAcquireFailure != null && breakAfterAcquireFailure) {
            dataSource.setBreakAfterAcquireFailure(true);
        }

        Integer timeout = config.getRemoveAbandonedTimeoutMillis() == null ? gConfig.getRemoveAbandonedTimeoutMillis()
                : config.getRemoveAbandonedTimeoutMillis();
        if (timeout != null) {
            dataSource.setRemoveAbandonedTimeoutMillis(timeout);
        }

        Boolean abandoned = config.getRemoveAbandoned() == null ? gConfig.getRemoveAbandoned() : config.getRemoveAbandoned();
        if (abandoned != null) {
            dataSource.setRemoveAbandoned(abandoned);
        }

        Boolean logAbandoned = config.getLogAbandoned() == null ? gConfig.getLogAbandoned() : config.getLogAbandoned();
        if (logAbandoned != null) {
            dataSource.setLogAbandoned(logAbandoned);
        }

        Integer queryTimeOut = config.getQueryTimeout() == null ? gConfig.getQueryTimeout() : config.getQueryTimeout();
        if (queryTimeOut != null) {
            dataSource.setQueryTimeout(queryTimeOut);
        }

        Integer transactionQueryTimeout =
                config.getTransactionQueryTimeout() == null ? gConfig.getTransactionQueryTimeout() : config.getTransactionQueryTimeout();
        if (transactionQueryTimeout != null) {
            dataSource.setTransactionQueryTimeout(transactionQueryTimeout);
        }
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return (type == null && druidExists) || (type != null && DRUID_DATASOURCE.equals(type.getName()));
    }
}
