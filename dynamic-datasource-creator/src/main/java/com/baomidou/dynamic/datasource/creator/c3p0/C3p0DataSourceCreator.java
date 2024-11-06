package com.baomidou.dynamic.datasource.creator.c3p0;

import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.enums.DdConstants;
import com.baomidou.dynamic.datasource.toolkit.ConfigMergeCreator;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.sql.DataSource;

/**
 * @author Joy
 */
@NoArgsConstructor
@AllArgsConstructor
public class C3p0DataSourceCreator implements DataSourceCreator {

    private static final ConfigMergeCreator<C3p0Config, ComboPooledDataSource> MERGE_CREATOR = new ConfigMergeCreator<>("C3p0", C3p0Config.class, ComboPooledDataSource.class);

    private C3p0Config c3p0Config;

    @SneakyThrows
    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        ComboPooledDataSource dataSource = MERGE_CREATOR.create(c3p0Config, dataSourceProperty.getC3p0());
        dataSource.setUser(dataSourceProperty.getUsername());
        dataSource.setJdbcUrl(dataSourceProperty.getUrl());
        dataSource.setPassword(dataSourceProperty.getPassword());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (DsStrUtils.hasText(driverClassName)) {
            dataSource.setDriverClass(driverClassName);
        }
        return dataSource;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return type == null || DdConstants.C3P0_DATASOURCE.equals(type.getName());
    }
}
