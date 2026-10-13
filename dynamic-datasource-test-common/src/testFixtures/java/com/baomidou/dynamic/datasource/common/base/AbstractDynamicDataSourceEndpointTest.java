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
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code /actuator/dynamic-datasource} 端点的跨版本契约：bean 由自动装配注册（依赖各 starter 的注册
 * 文件与 actuator 测试运行时），summary 反映 primary 开关、全部数据源的真实池类型与分组构成。
 * <p>
 * 版本无关：Boot2/3/4 的 starter 各以薄子类继承本类。
 *
 * @author TaoYu
 */
@SpringBootTest(classes = AutoConfigurationApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.datasource.dynamic.primary=master",
                "spring.datasource.dynamic.datasource.master.type=com.zaxxer.hikari.HikariDataSource",
                "spring.datasource.dynamic.datasource.master.driver-class-name=org.h2.Driver",
                "spring.datasource.dynamic.datasource.master.url=jdbc:h2:mem:ep_master;DB_CLOSE_DELAY=-1",
                "spring.datasource.dynamic.datasource.master.username=sa",
                "spring.datasource.dynamic.datasource.slave_1.type=com.zaxxer.hikari.HikariDataSource",
                "spring.datasource.dynamic.datasource.slave_1.driver-class-name=org.h2.Driver",
                "spring.datasource.dynamic.datasource.slave_1.url=jdbc:h2:mem:ep_slave1;DB_CLOSE_DELAY=-1",
                "spring.datasource.dynamic.datasource.slave_1.username=sa",
                // endpoint 未暴露时 @ConditionalOnAvailableEndpoint 可能不注册（取决于 Boot 版本），显式暴露保证可用
                "management.endpoints.web.exposure.include=dynamic-datasource"
        })
public abstract class AbstractDynamicDataSourceEndpointTest {

    @Autowired
    DynamicDataSourceEndpoint endpoint;
    @Autowired
    DataSource dataSource;

    @Test
    @SuppressWarnings("unchecked")
    public void endpointShouldBeRegisteredAndSummarizeRouting() {
        Map<String, Object> summary = endpoint.summary();
        assertThat(summary).containsEntry("primary", "master");
        assertThat(summary).containsEntry("strict", false);
        assertThat(summary).containsKeys("p6spy", "seata");

        DynamicRoutingDataSource routing = (DynamicRoutingDataSource) dataSource;
        Map<String, Object> dataSources = (Map<String, Object>) summary.get("dataSources");
        // 输出必须与路由实际状态一致；共享测试上下文中可能还有其他 provider 加载的数据源
        assertThat(dataSources.keySet()).isEqualTo(routing.getDataSources().keySet());
        assertThat(dataSources).containsKeys("master", "slave_1");
        // 通过 ItemDataSource 拆箱后反映真实池类型（slave_1 不会被其他 provider 覆盖）
        assertThat(dataSources.get("slave_1")).isEqualTo("HikariDataSource");

        Map<String, Object> groups = (Map<String, Object>) summary.get("groups");
        assertThat(groups.keySet()).isEqualTo(routing.getGroupDataSources().keySet());
        assertThat(groups).containsKey("slave");
        Map<String, Object> slave = (Map<String, Object>) groups.get("slave");
        assertThat(slave).containsEntry("size", 1);
        assertThat((List<String>) slave.get("dataSources")).containsExactly("slave_1");
    }
}
