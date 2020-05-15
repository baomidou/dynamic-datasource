package com.baomidou.dynamic.datasource.creator;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.dbcp.DbcpConfig;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang.ObjectUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorFactory;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;

public class DbcpDataSourceCreator {

    private DbcpConfig dbcpConfig;

    public DbcpDataSourceCreator(final DbcpConfig dbcpConfig) {
        this.dbcpConfig = dbcpConfig;
    }

    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setUrl(dataSourceProperty.getUrl());
        dataSource.setDriverClassName(dataSourceProperty.getDriverClassName());

        DbcpConfig localConfig = dataSourceProperty.getDbcp();
        // 全局配置
        if (dbcpConfig != null) {
            populateProperties(dataSource, dbcpConfig);
        }
        // 本地配置
        if (localConfig != null) {
            populateProperties(dataSource, localConfig);
        }
        return dataSource;
    }

    private void populateProperties(BasicDataSource dataSource, DbcpConfig dbcpConfig) {
        BeanWrapper configWrapper = new BeanWrapperImpl(dbcpConfig);
        BeanWrapper dataSourceWrapper = PropertyAccessorFactory.forBeanPropertyAccess(dataSource);
        for (PropertyDescriptor propertyDescriptor : configWrapper.getPropertyDescriptors()) {
            String propertyName = propertyDescriptor.getName();
            if (dataSourceWrapper.isWritableProperty(propertyName)) {
                Object propertyValue = configWrapper.getPropertyValue(propertyName);
                if (propertyValue != null) {
                    dataSourceWrapper.setPropertyValue(propertyName, propertyValue);
                }
            }

        }
    }
}
