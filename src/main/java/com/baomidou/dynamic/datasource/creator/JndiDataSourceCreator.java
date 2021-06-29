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
package com.baomidou.dynamic.datasource.creator;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.sql.DataSource;

/**
 * JNDI数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/27
 */
public class JndiDataSourceCreator extends AbstractDataSourceCreator implements DataSourceCreator {

    private static final JndiDataSourceLookup LOOKUP = new JndiDataSourceLookup();

    public JndiDataSourceCreator(DynamicDataSourceProperties dynamicDataSourceProperties) {
        super(dynamicDataSourceProperties);
    }

    public DataSource createDataSource(String jndiName) {
        return LOOKUP.getDataSource(jndiName);
    }

    /**
     * 创建JNDI数据源
     *
     * @param dataSourceProperty jndi数据源名称
     * @return 数据源
     */
    @Override
    public DataSource doCreateDataSource(DataSourceProperty dataSourceProperty) {
        return createDataSource(dataSourceProperty.getJndiName());
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        String jndiName = dataSourceProperty.getJndiName();
        return jndiName != null && !jndiName.isEmpty();
    }
}
