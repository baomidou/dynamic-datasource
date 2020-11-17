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

import javax.sql.DataSource;

/**
 *
 * 默认按照以下顺序创建数据源:
 * <pre>
 *	JNDI(1000) > DRUID(2000) > HIKARI(3000) > BASIC(5000)
 * </pre>
 * @author ls9527
 */
public interface DataSourceCreator {
    int JNDI_ORDER = 1000;

    int DEFAULT_ORDER = 5000;

    /**
     * 通过属性创建数据源
     * @param dataSourceProperty
     * @return
     */
    DataSource createDataSource(DataSourceProperty dataSourceProperty);
    /**
     * 通过属性创建数据源
     *
     * @param dataSourceProperty
     * @param publicKey
     * @return
     */
    DataSource createDataSource(DataSourceProperty dataSourceProperty, String publicKey);

    /**
     * 当前创建器是否支持根据此属性创建
     * @param dataSourceProperty
     * @return
     */
    boolean support(DataSourceProperty dataSourceProperty);
}
