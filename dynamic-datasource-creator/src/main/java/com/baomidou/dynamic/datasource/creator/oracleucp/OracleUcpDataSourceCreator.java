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
package com.baomidou.dynamic.datasource.creator.oracleucp;

import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.enums.DdConstants;
import com.baomidou.dynamic.datasource.exception.ErrorCreateDataSourceException;
import com.baomidou.dynamic.datasource.toolkit.ConfigMergeCreator;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import oracle.ucp.ConnectionAffinityCallback;
import oracle.ucp.ConnectionLabelingCallback;
import oracle.ucp.jdbc.ConnectionInitializationCallback;
import oracle.ucp.jdbc.PoolDataSourceImpl;

import javax.sql.DataSource;

/**
 * OracleUCP数据源创建器
 *
 * @author Shaoyu Liu
 */
@NoArgsConstructor
@AllArgsConstructor
public class OracleUcpDataSourceCreator implements DataSourceCreator {

    private static final ConfigMergeCreator<OracleUcpConfig, PoolDataSourceImpl> MERGE_CREATOR = new ConfigMergeCreator<>("oracleucp", OracleUcpConfig.class, PoolDataSourceImpl.class);
    private OracleUcpConfig oracleUcpConfig;

    @SneakyThrows
    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        PoolDataSourceImpl dataSource = MERGE_CREATOR.create(oracleUcpConfig, dataSourceProperty.getOracleUcp());
        dataSource.setURL(dataSourceProperty.getUrl());
        dataSource.setUser(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setConnectionPoolName(dataSourceProperty.getPoolName());
        OracleUcpConfig oracleUcp = dataSourceProperty.getOracleUcp();
        // 对特殊值做映射
        mappingSpecialValue(oracleUcp, dataSource);
        // 注册回调函数
        registerCallback(oracleUcp, dataSource);
        // 默认非时序数据库
        dataSource.setConnectionFactoryClassName(DsStrUtils.defaultIfBlank(oracleUcp.getConnectionFactoryClassName(),
                "oracle.jdbc.pool.OracleDataSource"));
        if (Boolean.FALSE.equals(dataSourceProperty.getLazy())) {
            try {
                dataSource.startPool();
            } catch (Exception e) {
                throw new ErrorCreateDataSourceException(
                        "dynamic-datasource create datasource named [" + dataSourceProperty.getPoolName() + "] error", e);
            }
        }
        return dataSource;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return type == null || DdConstants.ORACLE_UCP_DATASOURCE.equals(type.getName());
    }


    /**
     * 对特殊值做映射
     *
     * @param consumeConfig 自定义配置
     * @param targetConfig  目标配置
     */
    @SneakyThrows
    public static void mappingSpecialValue(OracleUcpConfig consumeConfig, PoolDataSourceImpl targetConfig) {
        // 处理配置名称和属性不一致情况
        if (consumeConfig.getConnectionFactoryProperty() != null) {
            targetConfig.setConnectionFactoryProperties(consumeConfig.getConnectionFactoryProperty());
        }
        if (consumeConfig.getConnectionProperty() != null) {
            targetConfig.setConnectionProperties(consumeConfig.getConnectionProperty());
        }

        // 处理多种配置对应一种属性，具体生效顺序和已和官方实现对齐，@see oracle.ucp.jdbc.PoolDataSourceImpl#applyPoolProperties(Map)
        if (consumeConfig.getMaxIdleTime() != null) {
            targetConfig.setInactiveConnectionTimeout(consumeConfig.getMaxIdleTime());
        }
        if (consumeConfig.getInactiveConnectionTimeout() != null) {
            targetConfig.setInactiveConnectionTimeout(consumeConfig.getInactiveConnectionTimeout());
        }
        if (consumeConfig.getTimeoutCheckInterval() != null) {
            targetConfig.setTimeoutCheckInterval(consumeConfig.getTimeoutCheckInterval());
        }
        if (consumeConfig.getPropertyCycle() != null) {
            targetConfig.setTimeoutCheckInterval(consumeConfig.getPropertyCycle());
        }
    }

    /**
     * 注册回调函数
     *
     * @param consumeConfig 自定义配置
     * @param targetConfig  目标配置
     */
    @SneakyThrows
    private static void registerCallback(OracleUcpConfig consumeConfig, PoolDataSourceImpl targetConfig) {
        String connectionLabelingCallbackClassName = consumeConfig.getConnectionLabelingCallbackClassName();
        if (DsStrUtils.hasText(connectionLabelingCallbackClassName)) {
            Class<?> labelingClass = Class.forName(connectionLabelingCallbackClassName);
            ConnectionLabelingCallback labelingObject = (ConnectionLabelingCallback) labelingClass.newInstance();
            if (labelingObject == null) {
                throw new IllegalArgumentException("param [connection-labeling-callback] has to be non-null");
            }
            targetConfig.registerConnectionLabelingCallback(labelingObject);
        }

        String connectionAffinityCallbackClassClassName = consumeConfig.getConnectionAffinityCallbackClassName();
        if (DsStrUtils.hasText(connectionAffinityCallbackClassClassName)) {
            Class<?> affinityClass = Class.forName(connectionAffinityCallbackClassClassName);
            ConnectionAffinityCallback affinityObject = (ConnectionAffinityCallback) affinityClass.newInstance();
            if (affinityObject == null) {
                throw new IllegalArgumentException("param [connection-initialization-callback] has to be non-null");
            }
            targetConfig.registerConnectionAffinityCallback(affinityObject);
        }

        String connectionInitializationCallbackClassName = consumeConfig.getConnectionInitializationCallbackClassName();
        if (DsStrUtils.hasText(connectionInitializationCallbackClassName)) {
            Class<?> initializationClass = Class.forName(connectionInitializationCallbackClassName);
            ConnectionInitializationCallback initializationObject = (ConnectionInitializationCallback) initializationClass.newInstance();
            if (initializationObject == null) {
                throw new IllegalArgumentException("param [connection-initialization-callback] has to be non-null");
            }
            targetConfig.registerConnectionInitializationCallback(initializationObject);
        }
    }
}