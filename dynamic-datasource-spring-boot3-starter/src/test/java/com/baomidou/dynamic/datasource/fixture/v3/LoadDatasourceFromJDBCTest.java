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
package com.baomidou.dynamic.datasource.fixture.v3;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.provider.AbstractJdbcDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = LoadDatasourceFromJDBCApplication.class, webEnvironment = RANDOM_PORT)
public class LoadDatasourceFromJDBCTest {

    @Autowired
    DataSource dataSource;

    @Test
    void testExistDataSource() {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        assertThat(ds.getDataSources().keySet()).contains("master", "db1", "db2", "db3");
    }
}

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
@SpringBootApplication
class LoadDatasourceFromJDBCApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadDatasourceFromJDBCApplication.class, args);
    }

    @Bean
    public DynamicDataSourceProvider dynamicDataSourceProvider(DefaultDataSourceCreator dataSourceCreator) {
        return new AbstractJdbcDataSourceProvider(dataSourceCreator, "org.h2.Driver", "jdbc:h2:mem:test", "sa", "") {
            @Override
            protected Map<String, DataSourceProperty> executeStmt(Statement statement) throws SQLException {
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS `DB`
                        (
                            `name`   VARCHAR(30) NULL DEFAULT NULL,
                            `username`   VARCHAR(30) NULL DEFAULT NULL,
                            `password`   VARCHAR(30) NULL DEFAULT NULL,
                            `url`   VARCHAR(30) NULL DEFAULT NULL,
                            `driver`   VARCHAR(30) NULL DEFAULT NULL
                        )""");
                statement.executeUpdate("insert into DB values ('master','sa','','jdbc:h2:~/test','org.h2.Driver')");
                statement.executeUpdate("insert into DB values ('db1','sa','','jdbc:h2:mem:test2','org.h2.Driver')");
                statement.executeUpdate("insert into DB values ('db2','sa','','jdbc:h2:mem:test3','org.h2.Driver')");
                statement.executeUpdate("insert into DB values ('db3','sa','','jdbc:h2:mem:test4','org.h2.Driver')");
                Map<String, DataSourceProperty> map = new HashMap<>();
                ResultSet rs = statement.executeQuery("select * from DB");
                while (rs.next()) {
                    DataSourceProperty dataSourceProperty = new DataSourceProperty();
                    dataSourceProperty.setUsername(rs.getString("username"));
                    dataSourceProperty.setPassword(rs.getString("password"));
                    dataSourceProperty.setUrl(rs.getString("url"));
                    dataSourceProperty.setDriverClassName(rs.getString("driver"));
                    dataSourceProperty.setType(SimpleDriverDataSource.class);
                    map.put(rs.getString("name"), dataSourceProperty);
                }
                return map;
            }
        };
    }
}
