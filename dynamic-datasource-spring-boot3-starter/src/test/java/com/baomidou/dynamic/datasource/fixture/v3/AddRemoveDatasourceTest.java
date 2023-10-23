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
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariCpConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = AddRemoveDatasourceApplication.class, webEnvironment = RANDOM_PORT)
public class AddRemoveDatasourceTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    DefaultDataSourceCreator dataSourceCreator;

    @Test
    void testAddAndRemoveDataSource() {
        HikariCpConfig hikariCpConfig = new HikariCpConfig();
        hikariCpConfig.setConnectionTestQuery("select 1");
        DataSourceProperty dataSourceProperty = new DataSourceProperty();
        dataSourceProperty.setHikari(hikariCpConfig);
        dataSourceProperty.setPoolName("slave_1");
        dataSourceProperty.setUsername("sa");
        dataSourceProperty.setPassword("");
        dataSourceProperty.setType(SimpleDriverDataSource.class);
        dataSourceProperty.setUrl("jdbc:h2:mem:test1");
        dataSourceProperty.setDriverClassName("org.h2.Driver");
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        // async destroy datasource
        ds.setGraceDestroy(true);
        ds.addDataSource(dataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(dataSourceProperty));
        assertThat(ds.getDataSources().keySet()).contains("slave_1");
        ds.removeDataSource("slave_1");
        // close directly
        ds.setGraceDestroy(false);
        ds.addDataSource(dataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(dataSourceProperty));
        assertThat(ds.getDataSources().keySet()).contains("slave_1");
        ds.removeDataSource("slave_1");
        assertThat(ds.getDataSources().keySet()).doesNotContain("slave_1");
    }
}

@SpringBootApplication
class AddRemoveDatasourceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AddRemoveDatasourceApplication.class, args);
    }
}
