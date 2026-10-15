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

import com.baomidou.dynamic.datasource.ds.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A test-only proxy with no close method and no JDBC unwrap support for its target.
 */
public class LifecycleProxyDataSource extends AbstractDataSource {

    private final DataSource targetDataSource;

    public LifecycleProxyDataSource(DataSource targetDataSource) {
        this.targetDataSource = targetDataSource;
    }

    public DataSource getTargetDataSource() {
        return targetDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return targetDataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return targetDataSource.getConnection(username, password);
    }
}
