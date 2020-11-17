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
 * 默认创建数据源无参的调用有参的
 *
 * @author ls9527
 */
public abstract class AbstractDataSourceCreator implements DataSourceCreator {

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        return createDataSource(dataSourceProperty, null);
    }
}
