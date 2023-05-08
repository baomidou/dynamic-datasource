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

import com.baomidou.dynamic.datasource.common.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.AbstractDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.enums.XADataSourceEnum;
import com.baomidou.dynamic.datasource.toolkit.ConfigMergeCreator;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;

import javax.sql.DataSource;
import java.util.Properties;

import static com.baomidou.dynamic.datasource.support.DdConstants.ATOMIKOS_DATASOURCE;

/**
 * Atomikos数据源配置
 *
 * @author <a href="mailto:312290710@qq.com">jiazhifeng</a>
 * @date 2023/03/02 10:20
 */
public class AtomikosDataSourceCreator extends AbstractDataSourceCreator implements DataSourceCreator, InitializingBean {
    private static final ConfigMergeCreator<AtomikosConfig, AtomikosConfig> MERGE_CREATOR = new ConfigMergeCreator<>("AtomikosConfig", AtomikosConfig.class, AtomikosConfig.class);
    private AtomikosConfig atomikosConfig;

    @Override
    public DataSource doCreateDataSource(DataSourceProperty dataSourceProperty) {
        AtomikosConfig config = MERGE_CREATOR.create(atomikosConfig, dataSourceProperty.getAtomikos());
        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();

        DbType dbType = JdbcUtils.getDbType(dataSourceProperty.getUrl());
        xaDataSource.setXaDataSourceClassName(XADataSourceEnum.getByDbType(dbType));

        Properties xaProperties = new Properties();
        xaProperties.setProperty("url", dataSourceProperty.getUrl());
        xaProperties.setProperty("user", dataSourceProperty.getUsername());
        xaProperties.setProperty("password", dataSourceProperty.getPassword());
        xaDataSource.setXaProperties(xaProperties);

        xaDataSource.setUniqueResourceName(dataSourceProperty.getPoolName());
        xaDataSource.setMinPoolSize(config.getMinPoolSize());
        xaDataSource.setMaxPoolSize(config.getMaxPoolSize());
        xaDataSource.setBorrowConnectionTimeout(config.getBorrowConnectionTimeout());
        xaDataSource.setReapTimeout(config.getReapTimeout());
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
        DbType dbType = JdbcUtils.getDbType(dataSourceProperty.getUrl());
        return (type == null || ATOMIKOS_DATASOURCE.equals(type.getName())) && XADataSourceEnum.contains(dbType);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        atomikosConfig = properties.getAtomikos();
    }
}