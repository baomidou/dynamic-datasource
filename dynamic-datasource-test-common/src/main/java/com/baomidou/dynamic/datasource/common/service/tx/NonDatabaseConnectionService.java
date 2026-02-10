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
package com.baomidou.dynamic.datasource.common.service.tx;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.tx.DsPropagation;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test service to reproduce the NPE issue when REQUIRES_NEW has no JDBC connections
 */
@Service
@DS("order")
public class NonDatabaseConnectionService {
    private final DataSource dataSource;
    private final NoConnectionService noConnectionService;

    public NonDatabaseConnectionService(DataSource dataSource, NoConnectionService noConnectionService) {
        this.dataSource = dataSource;
        this.noConnectionService = noConnectionService;
    }

    /**
     * Outer REQUIRED transaction with JDBC connection
     */
    @DSTransactional(propagation = DsPropagation.REQUIRED)
    public void outerRequiredWithConnection() {
        // Trigger JDBC connection
        triggerJdbcConnection();
        // Call nested REQUIRES_NEW without JDBC connection
        noConnectionService.innerRequiresNewWithoutConnection();
    }

    /**
     * Trigger a JDBC connection
     */
    private void triggerJdbcConnection() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM p_order")) {
            if (resultSet.next()) {
                resultSet.getInt(1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
