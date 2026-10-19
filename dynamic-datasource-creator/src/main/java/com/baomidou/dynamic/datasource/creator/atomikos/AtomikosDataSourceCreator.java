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
package com.baomidou.dynamic.datasource.creator.atomikos;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.enums.DdConstants;
import com.baomidou.dynamic.datasource.enums.XADataSourceEnum;
import com.baomidou.dynamic.datasource.exception.ErrorCreateDataSourceException;
import com.baomidou.dynamic.datasource.toolkit.ConfigMergeCreator;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;


/**
 * Atomikos数据源配置
 *
 * @author <a href="mailto:312290710@qq.com">jiazhifeng</a>
 */
@NoArgsConstructor
@AllArgsConstructor
public class AtomikosDataSourceCreator implements DataSourceCreator {
    private static final ConfigMergeCreator<AtomikosConfig, AtomikosConfig> MERGE_CREATOR = new ConfigMergeCreator<>("AtomikosConfig", AtomikosConfig.class, AtomikosConfig.class);
    /**
     * Atomikos 4/5 提供 setReapTimeout(int)，Atomikos 6.0.0 起（jakarta 版本线的起点）该方法被移除。
     * 本模块按 Atomikos 4.0.6 编译，编译期发现不了缺失，运行期直接调会在 Atomikos 6 下抛 NoSuchMethodError，
     * 应用无法启动。这里只在方法存在时调用，Atomikos 4/5 行为不变，Atomikos 6 下 reapTimeout 不生效但可创建数据源。
     */
    private static final Method REAP_TIMEOUT_METHOD = fetchReapTimeoutMethod();

    private AtomikosConfig atomikosConfig;

    private static Method fetchReapTimeoutMethod() {
        try {
            return AtomikosDataSourceBean.class.getMethod("setReapTimeout", int.class);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        AtomikosConfig config = MERGE_CREATOR.create(atomikosConfig, dataSourceProperty.getAtomikos());
        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();

        xaDataSource.setXaDataSourceClassName(dataSourceProperty.getDriverClassName());

        Properties xaProperties = new Properties();
        xaProperties.setProperty("url", dataSourceProperty.getUrl());
        xaProperties.setProperty("user", dataSourceProperty.getUsername());
        xaProperties.setProperty("password", dataSourceProperty.getPassword());
        xaDataSource.setXaProperties(xaProperties);

        xaDataSource.setUniqueResourceName(dataSourceProperty.getPoolName());
        xaDataSource.setMinPoolSize(config.getMinPoolSize());
        xaDataSource.setMaxPoolSize(config.getMaxPoolSize());
        xaDataSource.setBorrowConnectionTimeout(config.getBorrowConnectionTimeout());
        if (REAP_TIMEOUT_METHOD != null) {
            try {
                REAP_TIMEOUT_METHOD.invoke(xaDataSource, config.getReapTimeout());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ErrorCreateDataSourceException("AtomikosDataSourceBean failed to set reapTimeout", e);
            }
        }
        xaDataSource.setMaxIdleTime(config.getMaxIdleTime());
        xaDataSource.setTestQuery(config.getTestQuery());
        xaDataSource.setMaintenanceInterval(config.getMaintenanceInterval());
        xaDataSource.setDefaultIsolationLevel(config.getDefaultIsolationLevel());
        xaDataSource.setMaxLifetime(config.getMaxLifetime());
        return xaDataSource;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return (type == null || DdConstants.ATOMIKOS_DATASOURCE.equals(type.getName())) && XADataSourceEnum.contains(dataSourceProperty.getDriverClassName());
    }
}