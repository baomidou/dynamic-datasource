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
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

import javax.sql.DataSource;

import static com.baomidou.dynamic.datasource.creator.DataSourceCreator.JNDI_ORDER;

/**
 * JNDI数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/27
 */
@Order(JNDI_ORDER)
public class JndiDataSourceCreator extends AbstractDataSourceCreator implements DataSourceCreator {

    private static final JndiDataSourceLookup LOOKUP = new JndiDataSourceLookup();


    /**
     * 创建JNDI数据源
     *
     * @param dataSourceProperty jndi数据源名称
     * @param publicKey publicKey
     * @return 数据源
     */
    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty, String publicKey) {
        return LOOKUP.getDataSource(dataSourceProperty.getJndiName());
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        String jndiName = dataSourceProperty.getJndiName();
        return jndiName != null && !jndiName.isEmpty();
    }
}
