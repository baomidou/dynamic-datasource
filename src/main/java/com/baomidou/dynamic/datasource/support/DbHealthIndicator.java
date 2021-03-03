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
package com.baomidou.dynamic.datasource.support;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 数据库健康状况指标
 *
 * @author hubin
 */
public class DbHealthIndicator extends AbstractHealthIndicator {

    private final String validQuery;

    private final HealthCheckAdapter healthCheckAdapter;
    /**
     * 当前执行数据源
     */
    private final DataSource dataSource;

    public DbHealthIndicator(DataSource dataSource, String validQuery, HealthCheckAdapter healthCheckAdapter) {
        this.dataSource = dataSource;
        this.validQuery = validQuery;
        this.healthCheckAdapter = healthCheckAdapter;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (dataSource instanceof DynamicRoutingDataSource) {
            Map<String, DataSource> dataSourceMap = ((DynamicRoutingDataSource) dataSource).getCurrentDataSources();
            // 循环检查当前数据源是否可用
            Boolean available = null;
            Boolean disable = null;
            for (Map.Entry<String, DataSource> dataSource : dataSourceMap.entrySet()) {
                Boolean resultAvailable = false;
                try {
                    resultAvailable = queryAvailable(dataSource.getValue());
                } catch (Throwable ignore) {
                } finally {
                    healthCheckAdapter.putHealth(dataSource.getKey(), resultAvailable);
                    builder.withDetail(dataSource.getKey(), resultAvailable);

                    if (resultAvailable) {
                        available = true;
                    } else {
                        disable = true;
                    }
                }
            }
            if (available != null) {
                if (disable != null) {
                    builder.status(Status.OUT_OF_SERVICE);
                } else {
                    builder.status(Status.UP);
                }
            } else {
                builder.status(Status.DOWN);
            }

        }
    }


    private Boolean queryAvailable(DataSource dataSource) {
        List<Integer> results = new JdbcTemplate(dataSource).query(this.validQuery, new RowMapper<Integer>() {

            @Override
            public Integer mapRow(ResultSet resultSet, int i) throws SQLException {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columns = metaData.getColumnCount();
                if (columns != 1) {
                    throw new IncorrectResultSetColumnCountException(1, columns);
                }
                return (Integer) JdbcUtils.getResultSetValue(resultSet, 1, Integer.class);
            }
        });
        return DataAccessUtils.requiredSingleResult(results) == 1;
    }
}