package com.baomidou.dynamic.datasource.test.javax;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.hikari.HikariCpConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

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
        DataSourceProperty dataSourceProperty = new DataSourceProperty()
                .setPoolName("slave_1").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test1;MODE=MySQL")
                .setUsername("sa").setPassword("").setHikari(hikariCpConfig);
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
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
