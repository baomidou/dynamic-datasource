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
package com.baomidou.dynamic.datasource.toolkit;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidDataSourceProperties;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidGlobalDataSourceProperties;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * 数据源工厂
 *
 * @author TaoYu
 * @since 2.1.0
 */
public class DataSourceFactory {

    /**
     * Ali DruidD 数据源
     */
    public static final String DRUID_DATASOURCE = "com.alibaba.druid.pool.DruidDataSource";

    private static Method createMethod;
    private static Method typeMethod;
    private static Method urlMethod;
    private static Method usernameMethod;
    private static Method passwordMethod;
    private static Method driverClassNameMethod;
    private static Method buildMethod;

    static {
        Class builderClass = null;
        try {
            builderClass = Class.forName("org.springframework.boot.jdbc.DataSourceBuilder");
        } catch (Exception e) {
            try {
                builderClass = Class.forName("org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder");
            } catch (Exception e1) {
            }
        }
        try {
            createMethod = builderClass.getDeclaredMethod("create");
            typeMethod = builderClass.getDeclaredMethod("type", Class.class);
            urlMethod = builderClass.getDeclaredMethod("url", String.class);
            usernameMethod = builderClass.getDeclaredMethod("username", String.class);
            passwordMethod = builderClass.getDeclaredMethod("password", String.class);
            driverClassNameMethod = builderClass.getDeclaredMethod("driverClassName", String.class);
            buildMethod = builderClass.getDeclaredMethod("build");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建数据源
     *
     * @param dataSourceProperty 数据源信息
     * @return 数据源
     */
    public static DataSource createDataSource(DataSourceProperty dataSourceProperty, DruidGlobalDataSourceProperties druidDataSourceProperties) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        if (type == null) {
            try {
                Class.forName(DRUID_DATASOURCE);
                return createDruidDataSource(dataSourceProperty, druidDataSourceProperties);
            } catch (ClassNotFoundException e) {
            }
        } else if (DRUID_DATASOURCE.equals(type.getName())) {
            return createDruidDataSource(dataSourceProperty, druidDataSourceProperties);
        }
        return createBasicDataSource(dataSourceProperty);
    }

    public static DataSource createBasicDataSource(DataSourceProperty dataSourceProperty) {
        try {
            Object o1 = createMethod.invoke(null);
            Object o2 = typeMethod.invoke(o1, dataSourceProperty.getType());
            Object o3 = urlMethod.invoke(o2, dataSourceProperty.getUrl());
            Object o4 = usernameMethod.invoke(o3, dataSourceProperty.getUsername());
            Object o5 = passwordMethod.invoke(o4, dataSourceProperty.getPassword());
            Object o6 = driverClassNameMethod.invoke(o5, dataSourceProperty.getDriverClassName());
            return (DataSource) buildMethod.invoke(o6);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DataSource createDruidDataSource(DataSourceProperty dataSourceProperty, DruidGlobalDataSourceProperties druid) {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(dataSourceProperty.getUrl());
        druidDataSource.setUsername(dataSourceProperty.getUsername());
        druidDataSource.setPassword(dataSourceProperty.getPassword());
        druidDataSource.setDriverClassName(dataSourceProperty.getDriverClassName());

        DruidDataSourceProperties properties = dataSourceProperty.getDruid();

        druidDataSource.setInitialSize(properties.getInitialSize() != null ? properties.getInitialSize() : druid.getInitialSize());

        druidDataSource.setMaxActive(properties.getMaxActive() != null ? properties.getMaxActive() : druid.getMaxActive());
        druidDataSource.setMinIdle(properties.getMinIdle() != null ? properties.getMinIdle() : druid.getMinIdle());
        druidDataSource.setMaxWait(properties.getMaxWait() != null ? properties.getMaxWait() : druid.getMaxWait());
        druidDataSource.setTimeBetweenEvictionRunsMillis(properties.getTimeBetweenEvictionRunsMillis() != null ? properties.getTimeBetweenEvictionRunsMillis() : druid.getTimeBetweenEvictionRunsMillis());
        druidDataSource.setMinEvictableIdleTimeMillis(properties.getMinEvictableIdleTimeMillis() != null ? properties.getMinEvictableIdleTimeMillis() : druid.getMinEvictableIdleTimeMillis());
        druidDataSource.setMaxEvictableIdleTimeMillis(properties.getMaxEvictableIdleTimeMillis() != null ? properties.getMaxEvictableIdleTimeMillis() : druid.getMaxEvictableIdleTimeMillis());
        druidDataSource.setValidationQuery(properties.getValidationQuery() != null ? properties.getValidationQuery() : druid.getValidationQuery());
        druidDataSource.setValidationQueryTimeout(properties.getValidationQueryTimeout() != null ? properties.getValidationQueryTimeout() : druid.getValidationQueryTimeout());
        druidDataSource.setTestOnBorrow(properties.getTestOnBorrow() != null ? properties.getTestOnBorrow() : druid.isTestOnBorrow());
        druidDataSource.setTestOnReturn(properties.getTestOnReturn() != null ? properties.getTestOnReturn() : druid.isTestOnReturn());
        druidDataSource.setTestWhileIdle(properties.getTestWhileIdle() != null ? properties.getTestWhileIdle() : druid.isTestWhileIdle());
        druidDataSource.setPoolPreparedStatements(properties.getPoolPreparedStatements() != null ? properties.getPoolPreparedStatements() : druid.isPoolPreparedStatements());
        druidDataSource.setMaxOpenPreparedStatements(properties.getMaxOpenPreparedStatements() != null ? properties.getMaxOpenPreparedStatements() : druid.getMaxOpenPreparedStatements());
        druidDataSource.setSharePreparedStatements(properties.getSharePreparedStatements() != null ? properties.getSharePreparedStatements() : druid.isSharePreparedStatements());
        druidDataSource.setConnectProperties(properties.getConnectionProperties() != null ? properties.getConnectionProperties() : druid.getConnectionProperties());
        try {
            druidDataSource.setFilters(properties.getFilters() != null ? properties.getFilters() : druid.getFilters());
            druidDataSource.init();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return druidDataSource;
    }

}
