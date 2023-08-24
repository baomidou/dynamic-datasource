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
package com.baomidou.dynamic.datasource.creator.druid;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.CommonsLogFilter;
import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.enums.DdConstants;
import com.baomidou.dynamic.datasource.exception.ErrorCreateDataSourceException;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

/**
 * Druid数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/21
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class DruidDataSourceCreator implements DataSourceCreator {

    private static final Set<String> PARAMS = new HashSet<>();
    private static Method configMethod = null;

    static {
        fetchMethod();
    }

    static {
        PARAMS.add("defaultCatalog");
        PARAMS.add("defaultAutoCommit");
        PARAMS.add("defaultReadOnly");
        PARAMS.add("defaultTransactionIsolation");
        PARAMS.add("testOnReturn");
        PARAMS.add("validationQueryTimeout");
        PARAMS.add("sharePreparedStatements");
        PARAMS.add("connectionErrorRetryAttempts");
        PARAMS.add("breakAfterAcquireFailure");
        PARAMS.add("removeAbandonedTimeoutMillis");
        PARAMS.add("removeAbandoned");
        PARAMS.add("logAbandoned");
        PARAMS.add("queryTimeout");
        PARAMS.add("transactionQueryTimeout");
        PARAMS.add("timeBetweenConnectErrorMillis");
        PARAMS.add("connectTimeout");
        PARAMS.add("socketTimeout");
    }

    //    @Autowired(required = false)
//    private ApplicationContext applicationContext;
    private DruidConfig gConfig;

    /**
     * Druid since 1.2.17 use 'configFromPropeties' to copy config
     * Druid < 1.2.17 use 'configFromPropety' to copy config
     */
    private static void fetchMethod() {
        Class<DruidDataSource> aClass = DruidDataSource.class;
        try {
            configMethod = aClass.getMethod("configFromPropeties", Properties.class);
            return;
        } catch (NoSuchMethodException ignored) {
        }

        try {
            configMethod = aClass.getMethod("configFromPropety", Properties.class);
            return;
        } catch (NoSuchMethodException ignored) {
        }
        throw new RuntimeException("Druid does not has 'configFromPropeties' or 'configFromPropety' method!");
    }

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setUrl(dataSourceProperty.getUrl());
        dataSource.setName(dataSourceProperty.getPoolName());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (DsStrUtils.hasText(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }
        DruidConfig config = dataSourceProperty.getDruid();
        Properties properties = DruidConfigUtil.mergeConfig(gConfig, config);

        List<Filter> proxyFilters = this.initFilters(dataSourceProperty, properties.getProperty("druid.filters"));
        dataSource.setProxyFilters(proxyFilters);
        try {
            configMethod.invoke(dataSource, properties);
        } catch (Exception ignore) {

        }
        //连接参数单独设置
        dataSource.setConnectProperties(config.getConnectionProperties());
        //设置druid内置properties不支持的的参数
        for (String param : PARAMS) {
            DruidConfigUtil.setValue(dataSource, param, gConfig, config);
        }

        if (Boolean.FALSE.equals(dataSourceProperty.getLazy())) {
            try {
                dataSource.init();
            } catch (SQLException e) {
                throw new ErrorCreateDataSourceException("druid create error", e);
            }
        }
        return dataSource;
    }

    private List<Filter> initFilters(DataSourceProperty dataSourceProperty, String filters) {
        List<Filter> proxyFilters = new ArrayList<>(2);
        if (DsStrUtils.hasText(filters)) {
            String[] filterItems = filters.split(",");
            for (String filter : filterItems) {
                switch (filter) {
                    case "stat":
                        proxyFilters.add(DruidStatConfigUtil.toStatFilter(dataSourceProperty.getDruid().getStat(), gConfig.getStat()));
                        break;
                    case "wall":
                        WallConfig wallConfig = DruidWallConfigUtil.toWallConfig(dataSourceProperty.getDruid().getWall(), gConfig.getWall());
                        proxyFilters.add(DruidWallFilterUtil.toWallFilter(wallConfig, dataSourceProperty.getDruid().getWall(), gConfig.getWall()));
                        break;
                    case "slf4j":
                        proxyFilters.add(DruidLogConfigUtil.initFilter(Slf4jLogFilter.class, dataSourceProperty.getDruid().getSlf4j(), gConfig.getSlf4j()));
                        break;
                    case "commons-log":
                        proxyFilters.add(DruidLogConfigUtil.initFilter(CommonsLogFilter.class, dataSourceProperty.getDruid().getCommonsLog(), gConfig.getCommonsLog()));
                        break;
                    case "log4j":
                        proxyFilters.add(DruidLogConfigUtil.initFilter(Log4jFilter.class, dataSourceProperty.getDruid().getLog4j(), gConfig.getLog4j()));
                        break;
                    case "log4j2":
                        proxyFilters.add(DruidLogConfigUtil.initFilter(Log4j2Filter.class, dataSourceProperty.getDruid().getLog4j2(), gConfig.getLog4j2()));
                        break;
                    default:
                        log.warn("dynamic-datasource current not support [{}]", filter);
                }
            }
        }
//        if (this.applicationContext != null) {
//            for (String filterId : gConfig.getProxyFilters()) {
//                proxyFilters.add(this.applicationContext.getBean(filterId, Filter.class));
//            }
//        }
        return proxyFilters;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return type == null || DdConstants.DRUID_DATASOURCE.equals(type.getName());
    }
}
