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
package com.baomidou.dynamic.datasource.common.lifecycle;

import com.baomidou.dynamic.datasource.unwrapper.DataSourceUnwrapper;

import javax.sql.DataSource;

/**
 * A test-fixture service provider that only recognizes the dedicated lifecycle proxy.
 */
public class LifecycleDataSourceUnwrapper implements DataSourceUnwrapper {

    @Override
    public boolean support(DataSource dataSource) {
        return dataSource instanceof LifecycleProxyDataSource;
    }

    @Override
    public DataSource unwrap(DataSource dataSource) {
        return ((LifecycleProxyDataSource) dataSource).getTargetDataSource();
    }
}
