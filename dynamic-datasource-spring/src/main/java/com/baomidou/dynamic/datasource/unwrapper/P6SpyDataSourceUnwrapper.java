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

import com.p6spy.engine.spy.P6DataSource;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;

/**
 * Optional P6Spy integration. The proxy has no public target accessor.
 */
public class P6SpyDataSourceUnwrapper implements DataSourceUnwrapper {

    private static final boolean PRESENT = ClassUtils.isPresent(
            "com.p6spy.engine.spy.P6DataSource", P6SpyDataSourceUnwrapper.class.getClassLoader());

    @Override
    public boolean support(DataSource dataSource) {
        return PRESENT && dataSource instanceof P6DataSource;
    }

    @Override
    public DataSource unwrap(DataSource dataSource) throws ReflectiveOperationException {
        Field field = P6DataSource.class.getDeclaredField("realDataSource");
        field.setAccessible(true);
        return (DataSource) field.get(dataSource);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
