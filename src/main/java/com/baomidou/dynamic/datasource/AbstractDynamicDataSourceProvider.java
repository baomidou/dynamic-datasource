/**
 * Copyright © 2018 organization 苞米豆
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
package com.baomidou.dynamic.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DruidDataSourceProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicItemDataSourceProperties;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 抽象数据源，提供创建常用数据源的方法
 *
 * @author TaoYu
 * @since 1.1.0
 */
@Slf4j
public abstract class AbstractDynamicDataSourceProvider implements DynamicDataSourceProvider {

    public static final String DRUID_DATASOURCE = "com.alibaba.druid.pool.DruidDataSource";
    public static final String HIKARI_DATASOURCE = "com.zaxxer.hikari.HikariDataSource";

    protected DataSource createDataSource(DynamicItemDataSourceProperties properties) {
        Class<? extends DataSource> type = properties.getType();
        if (type == null) {
            try {
                Class.forName(DRUID_DATASOURCE);
                return createDruidDataSource(properties);
            } catch (ClassNotFoundException e) {
                log.debug("dynamic not found DruidDataSource");
            }
            try {
                Class.forName(HIKARI_DATASOURCE);
                return createHikariDataSource(properties);
            } catch (ClassNotFoundException e) {
                log.debug("dynamic not found HikariDataSource");
            }
            throw new RuntimeException("please set master and slave type like spring.dynamic.datasource.master.type");
        } else if (DRUID_DATASOURCE.equals(type.getName())) {
            return createDruidDataSource(properties);
        } else {
            return properties.initializeDataSourceBuilder().build();
        }
    }

    private DataSource createHikariDataSource(DynamicItemDataSourceProperties properties) {
        properties.setType(HikariDataSource.class);
        return properties.initializeDataSourceBuilder().build();
    }

    private DataSource createDruidDataSource(DynamicItemDataSourceProperties properties) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(properties.getUrl());
        druidDataSource.setUsername(properties.getUsername());
        druidDataSource.setPassword(properties.getPassword());
        druidDataSource.setDriverClassName(properties.getDriverClassName());

        DruidDataSourceProperties druidProperties = properties.getDruid();

        druidDataSource.setInitialSize(druidProperties.getInitialSize());
        druidDataSource.setMaxActive(druidProperties.getMaxActive());
        druidDataSource.setMinIdle(druidProperties.getMinIdle());
        druidDataSource.setMaxWait(druidProperties.getMaxWait());
        druidDataSource.setTimeBetweenEvictionRunsMillis(druidProperties.getTimeBetweenEvictionRunsMillis());
        druidDataSource.setMinEvictableIdleTimeMillis(druidProperties.getMinEvictableIdleTimeMillis());
        druidDataSource.setMaxEvictableIdleTimeMillis(druidProperties.getMaxEvictableIdleTimeMillis());
        druidDataSource.setValidationQuery(druidProperties.getValidationQuery());
        druidDataSource.setValidationQueryTimeout(druidProperties.getValidationQueryTimeout());
        druidDataSource.setTestOnBorrow(druidProperties.isTestOnBorrow());
        druidDataSource.setTestOnReturn(druidProperties.isTestOnReturn());
        druidDataSource.setPoolPreparedStatements(druidProperties.isPoolPreparedStatements());
        druidDataSource.setMaxOpenPreparedStatements(druidProperties.getMaxOpenPreparedStatements());
        druidDataSource.setSharePreparedStatements(druidProperties.isSharePreparedStatements());
        druidDataSource.setConnectProperties(druidProperties.getConnectionProperties());
        try {
            druidDataSource.setFilters(druidProperties.getFilters());
            druidDataSource.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return druidDataSource;
    }

}
