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
import com.baomidou.dynamic.datasource.fixture.v3.service.spel.User;
import com.baomidou.dynamic.datasource.fixture.v3.service.spel.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

/**
 * SpEL is not available under the nativeTest due to
 * <a href="https://github.com/spring-projects/spring-framework/issues/29548">Explore how to make
 * StandardBeanExpressionResolver not reachable in AOT mode</a>
 */
@DisabledInNativeImage
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
        DataSourceProperty masterDataSourceProperty = createDataSourceProperty("master");
        DataSourceProperty tenant1_1DataSourceProperty = createDataSourceProperty("tenant1_1");
        DataSourceProperty tenant1_2DataSourceProperty = createDataSourceProperty("tenant1_2");
        DataSourceProperty tenant2_1DataSourceProperty = createDataSourceProperty("tenant2_1");
        DataSourceProperty tenant2_2DataSourceProperty = createDataSourceProperty("tenant2_2");
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
            mockMvc.perform(MockMvcRequestBuilders.get("/users/header").contentType(MediaType.APPLICATION_FORM_URLENCODED)
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

    private DataSourceProperty createDataSourceProperty(String poolName) {
        DataSourceProperty result = new DataSourceProperty();
        result.setPoolName(poolName);
        result.setDriverClassName("org.h2.Driver");
        result.setUrl("jdbc:h2:mem:" + poolName + ";INIT=RUNSCRIPT FROM 'classpath:db/spring-expression-language.sql'");
        result.setUsername("sa");
        result.setPassword("");
        return result;
    }
}

@SpringBootApplication
class SPELApplication {
    public static void main(String[] args) {
        SpringApplication.run(SPELApplication.class, args);
    }
}
