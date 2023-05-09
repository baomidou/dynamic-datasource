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
package com.baomidou.dynamic.datasource.creator.dbcp;

import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.enums.DdConstants;
import com.baomidou.dynamic.datasource.toolkit.ConfigMergeCreator;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;

/**
 * DBCP数据源创建器
 *
 * @author TaoYu
 * @since 2021/5/18
 */
public class Dbcp2DataSourceCreator implements DataSourceCreator {

    private static final ConfigMergeCreator<Dbcp2Config, BasicDataSource> MERGE_CREATOR = new ConfigMergeCreator<>("Dbcp2", Dbcp2Config.class, BasicDataSource.class);

    private Dbcp2Config gConfig;

    @Override
    @SneakyThrows
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        BasicDataSource dataSource = MERGE_CREATOR.create(gConfig, dataSourceProperty.getDbcp2());
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setUrl(dataSourceProperty.getUrl());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (DsStrUtils.hasText(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }
        if (Boolean.FALSE.equals(dataSourceProperty.getLazy())) {
            dataSource.start();
        }
        return dataSource;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return type == null || DdConstants.DBCP2_DATASOURCE.equals(type.getName());
    }
}