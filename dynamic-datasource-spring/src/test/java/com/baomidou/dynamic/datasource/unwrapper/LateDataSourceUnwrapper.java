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
package com.baomidou.dynamic.datasource.unwrapper;

import javax.sql.DataSource;

/** Listed first in the service file, but must run after the successful provider. */
public class LateDataSourceUnwrapper implements DataSourceUnwrapper {

    @Override
    public boolean support(DataSource dataSource) {
        return dataSource instanceof TestDataSourceWrapper;
    }

    @Override
    public DataSource unwrap(DataSource dataSource) throws Exception {
        DataSource target = ((TestDataSourceWrapper) dataSource).getTarget();
        if (target != null && target != dataSource) {
            throw new AssertionError("Lower-order provider must run first");
        }
        return dataSource;
    }
}
