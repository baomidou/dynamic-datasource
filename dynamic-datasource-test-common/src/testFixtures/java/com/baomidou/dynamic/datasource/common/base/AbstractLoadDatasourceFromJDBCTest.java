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
package com.baomidou.dynamic.datasource.common.base;

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

/**
 * 从 JDBC 数据表加载数据源（{@link AbstractJdbcDataSourceProvider}）的共享集成场景。
 * <p>
 * 版本无关：Boot2/3/4 的 starter 各以薄子类继承本类，跑同一套断言。建表 SQL 使用字符串拼接
 * 而非文本块，因本 fixtures 源集以 JDK8 toolchain 编译（须兼容 Boot2 线）。
 *
 * @author TaoYu
 */
@SpringBootTest(classes = LoadDatasourceFromJDBCApplication.class, webEnvironment = RANDOM_PORT)
public abstract class AbstractLoadDatasourceFromJDBCTest {

    @Autowired
    DataSource dataSource;

    @Test
    void testExistDataSource() {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        assertThat(ds.getDataSources()).containsOnlyKeys("master", "db1", "db2", "db3");
    }
}

/**
 * 扫描范围不含各场景启动类所在的包，否则别的场景的启动类连同其 bean 会混进本场景的上下文（#812）。本类的 JDBC provider
 * 就曾这样混进其他场景，顶替了它们配置的 master。
 * <p>
 * provider 登记的库都是 H2 内存库，测试连上其中任何一个都不会在构建目录之外留下数据库文件。
 */
@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
@SpringBootApplication(scanBasePackages = "com.baomidou.dynamic.datasource.common.service")
class LoadDatasourceFromJDBCApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadDatasourceFromJDBCApplication.class, args);
    }

    @Bean
    public DynamicDataSourceProvider dynamicDataSourceProvider(DefaultDataSourceCreator dataSourceCreator) {
        return new AbstractJdbcDataSourceProvider(dataSourceCreator, "org.h2.Driver", "jdbc:h2:mem:test", "sa", "") {
            @Override
            protected Map<String, DataSourceProperty> executeStmt(Statement statement) throws SQLException {
                statement.execute("CREATE TABLE IF NOT EXISTS `DB`\n" +
                        "(\n" +
                        "    `name`   VARCHAR(30) NULL DEFAULT NULL,\n" +
                        "    `username`   VARCHAR(30) NULL DEFAULT NULL,\n" +
                        "    `password`   VARCHAR(30) NULL DEFAULT NULL,\n" +
                        "    `url`   VARCHAR(30) NULL DEFAULT NULL,\n" +
                        "    `driver`   VARCHAR(30) NULL DEFAULT NULL\n" +
                        ")");
                statement.executeUpdate("insert into DB values ('master','sa','','jdbc:h2:mem:test1','org.h2.Driver')");
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
