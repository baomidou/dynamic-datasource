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

import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;

/**
 * Optional Seata AT datasource proxy integration.
 */
public class SeataDataSourceUnwrapper implements DataSourceUnwrapper {

    private static final boolean PRESENT = ClassUtils.isPresent(
            "io.seata.rm.datasource.DataSourceProxy", SeataDataSourceUnwrapper.class.getClassLoader());

    @Override
    public boolean support(DataSource dataSource) {
        return PRESENT && dataSource instanceof DataSourceProxy;
    }

    @Override
    public DataSource unwrap(DataSource dataSource) {
        return ((DataSourceProxy) dataSource).getTargetDataSource();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
