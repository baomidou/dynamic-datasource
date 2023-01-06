package com.baomidou.dynamic.datasource.test.javax;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.test.javax.service.spel.User;
import com.baomidou.dynamic.datasource.test.javax.service.spel.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(classes = SPELApplication.class, webEnvironment = RANDOM_PORT)
public class SPELTest {
    MockMvc mockMvc;
    @Autowired
    DataSource dataSource;
    @Autowired
    DefaultDataSourceCreator dataSourceCreator;

    @Autowired
    UserService userService;

    @BeforeEach
    void setup(WebApplicationContext webApplicationContext) {
        this.mockMvc = webAppContextSetup(webApplicationContext).defaultResponseCharacterEncoding(StandardCharsets.UTF_8).build();
    }

    @Test
    void testSPEL() {
        DataSourceProperty masterDataSourceProperty = new DataSourceProperty()
                .setPoolName("master").setDriverClassName("org.h2.Driver")
                .setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE;INIT=RUNSCRIPT FROM 'classpath:db/spring-expression-language.sql'")
                .setUsername("sa").setPassword("");
        DataSourceProperty tenant1_1DataSourceProperty = new DataSourceProperty()
                .setPoolName("tenant1_1").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DataSourceProperty tenant1_2DataSourceProperty = new DataSourceProperty()
                .setPoolName("tenant1_2").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DataSourceProperty tenant2_1DataSourceProperty = new DataSourceProperty()
                .setPoolName("tenant2_1").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DataSourceProperty tenant2_2DataSourceProperty = new DataSourceProperty()
                .setPoolName("tenant2_2").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        ds.addDataSource(masterDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(masterDataSourceProperty));
        ds.addDataSource(tenant1_1DataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(tenant1_1DataSourceProperty));
        ds.addDataSource(tenant1_2DataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(tenant1_2DataSourceProperty));
        ds.addDataSource(tenant2_1DataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(tenant2_1DataSourceProperty));
        ds.addDataSource(tenant2_2DataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(tenant2_2DataSourceProperty));
        assertThat(ds.getDataSources().keySet()).contains("master", "tenant1_1", "tenant1_2", "tenant2_1", "tenant2_2");
        assertDoesNotThrow(() -> {
            mockMvc.perform(MockMvcRequestBuilders.get("/users/session").characterEncoding(StandardCharsets.UTF_8))
                    .andDo(print()).andExpectAll(
                            status().isOk(),
                            content().encoding(StandardCharsets.UTF_8)
                    ).andReturn().getResponse().getContentAsString();
            mockMvc.perform(MockMvcRequestBuilders.get("/users/header")
                    .header("tenantName", "tenant1")
                    .characterEncoding(StandardCharsets.UTF_8)
            ).andDo(print()).andExpectAll(
                    status().isOk(),
                    content().encoding(StandardCharsets.UTF_8)
            ).andReturn().getResponse().getContentAsString();
        });
        assertThat(userService.selectSpelByKey("tenant1")).isEqualTo("tenant1");
        assertThat(userService.selecSpelByTenant(new User("tenant2"))).isEqualTo("tenant2");
    }
}

@SpringBootApplication
class SPELApplication {
    public static void main(String[] args) {
        SpringApplication.run(SPELApplication.class, args);
    }
}