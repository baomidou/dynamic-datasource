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
package com.baomidou.dynamic.datasource.creator;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.dbcp.DbcpConfig;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.PropertyAccessorFactory;

import javax.sql.DataSource;
import java.beans.PropertyDescriptor;
/**
 * Dbcp数据源创建器
 *
 * @author liushang
 * @since 2020/1/21
 */
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
