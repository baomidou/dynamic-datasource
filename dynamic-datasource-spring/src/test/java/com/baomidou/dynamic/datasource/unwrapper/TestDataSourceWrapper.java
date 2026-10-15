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

import com.baomidou.dynamic.datasource.ds.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** Test-only proxy understood exclusively by the service provider. */
public class TestDataSourceWrapper extends AbstractDataSource {

    private DataSource target;
    private Exception failure;

    public TestDataSourceWrapper(DataSource target) {
        this.target = target;
    }

    public DataSource getTarget() throws Exception {
        if (failure != null) {
            throw failure;
        }
        return target;
    }

    public void setTarget(DataSource target) {
        this.target = target;
    }

    public void setFailure(Exception failure) {
        this.failure = failure;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return target.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return target.getConnection(username, password);
    }
}
