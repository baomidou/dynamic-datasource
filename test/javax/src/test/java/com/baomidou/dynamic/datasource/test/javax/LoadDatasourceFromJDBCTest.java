package com.baomidou.dynamic.datasource.test.javax;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.provider.AbstractJdbcDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

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
    public DynamicDataSourceProvider dynamicDataSourceProvider() {
        return new AbstractJdbcDataSourceProvider("org.h2.Driver", "jdbc:h2:mem:test;MODE=MySQL", "sa", "") {
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
                statement.executeUpdate("insert into DB values ('master','sa','','jdbc:h2:mem:test;MODE=MySQL','org.h2.Driver')");
                statement.executeUpdate("insert into DB values ('db1','sa','','jdbc:h2:mem:test2','org.h2.Driver')");
                statement.executeUpdate("insert into DB values ('db2','sa','','jdbc:h2:mem:test3','org.h2.Driver')");
                statement.executeUpdate("insert into DB values ('db3','sa','','jdbc:h2:mem:test4','org.h2.Driver')");
                Map<String, DataSourceProperty> map = new HashMap<>();
                ResultSet rs = statement.executeQuery("select * from DB");
                while (rs.next()) {
                    map.put(rs.getString("name"), new DataSourceProperty()
                            .setUsername(rs.getString("username")).setPassword(rs.getString("password"))
                            .setUrl(rs.getString("url")).setDriverClassName(rs.getString("driver")));
                }
                return map;
            }
        };
    }
}
