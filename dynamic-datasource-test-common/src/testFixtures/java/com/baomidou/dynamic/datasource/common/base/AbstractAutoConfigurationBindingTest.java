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
import com.baomidou.dynamic.datasource.ds.GroupDataSource;
import com.baomidou.dynamic.datasource.exception.CannotFindDataSourceException;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.strategy.RandomDynamicDataSourceStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 自动装配属性绑定的共享版本兼容场景：用同一套 {@code spring.datasource.dynamic.*} 配置（混合 kebab 与
 * camel 松散绑定写法）分别在 Boot2/3/4 上装配，断言 primary / strict / strategy / publicKey 及嵌套
 * datasource 分组都能一致地绑定并真正作用到 {@link DynamicRoutingDataSource}。
 * <p>
 * 版本无关：三个 starter 各以薄子类继承本类，从而验证 {@code @ConfigurationProperties} 绑定契约在不同
 * Spring Boot 大版本上的兼容性。本 fixtures 源集以 JDK8 toolchain 编译，故不依赖任何高版本语法。
 *
 * @author TaoYu
 */
@SpringBootTest(classes = AutoConfigurationApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                // 顶层属性：primary / strict / strategy / publicKey
                "spring.datasource.dynamic.primary=master",
                "spring.datasource.dynamic.strict=true",
                "spring.datasource.dynamic.strategy=com.baomidou.dynamic.datasource.strategy.RandomDynamicDataSourceStrategy",
                "spring.datasource.dynamic.publicKey=QODER-SHARED-PUBLIC-KEY",
                // 主库：kebab 松散绑定
                "spring.datasource.dynamic.datasource.master.driver-class-name=org.h2.Driver",
                "spring.datasource.dynamic.datasource.master.url=jdbc:h2:mem:ac_master;DB_CLOSE_DELAY=-1",
                "spring.datasource.dynamic.datasource.master.username=sa",
                // 从库分组：camel 松散绑定，slave_1 / slave_2 以下划线归入 "slave" 组
                "spring.datasource.dynamic.datasource.slave_1.driverClassName=org.h2.Driver",
                "spring.datasource.dynamic.datasource.slave_1.url=jdbc:h2:mem:ac_slave1;DB_CLOSE_DELAY=-1",
                "spring.datasource.dynamic.datasource.slave_1.username=sa",
                "spring.datasource.dynamic.datasource.slave_2.driverClassName=org.h2.Driver",
                "spring.datasource.dynamic.datasource.slave_2.url=jdbc:h2:mem:ac_slave2;DB_CLOSE_DELAY=-1",
                "spring.datasource.dynamic.datasource.slave_2.username=sa",
        })
public abstract class AbstractAutoConfigurationBindingTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    DynamicDataSourceProperties properties;

    @Test
    void topLevelPropertiesShouldBindConsistently() {
        assertThat(properties.getPrimary()).isEqualTo("master");
        assertThat(properties.getStrict()).isTrue();
        assertThat(properties.getStrategy()).isEqualTo(RandomDynamicDataSourceStrategy.class);
        assertThat(properties.getPublicKey()).isEqualTo("QODER-SHARED-PUBLIC-KEY");
    }

    @Test
    void nestedDatasourceMapShouldBindAllEntries() {
        assertThat(properties.getDatasource().keySet()).containsExactlyInAnyOrder("master", "slave_1", "slave_2");
        // 松散绑定：kebab 与 camel 两种写法都应解析到同一字段
        assertThat(properties.getDatasource().get("master").getDriverClassName()).isEqualTo("org.h2.Driver");
        assertThat(properties.getDatasource().get("slave_1").getDriverClassName()).isEqualTo("org.h2.Driver");
    }

    @Test
    void routingDataSourceShouldApplyPrimaryAndStrict() {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        // primary 生效：空 key 回落到 master
        assertThat(ds.getDataSource(null)).isNotNull();
        assertThat(ds.getDataSource("master")).isNotNull();
        // strict 生效：未匹配的数据源名直接报错而非回落
        assertThatThrownBy(() -> ds.getDataSource("does-not-exist"))
                .isInstanceOf(CannotFindDataSourceException.class);
    }

    @Test
    void groupRoutingShouldUseBoundStrategy() {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        assertThat(ds.getGroupDataSources()).containsKey("slave");
        GroupDataSource slave = ds.getGroupDataSources().get("slave");
        assertThat(slave.size()).isEqualTo(2);
        assertThat(slave.getDynamicDataSourceStrategy()).isInstanceOf(RandomDynamicDataSourceStrategy.class);
    }
}

@SpringBootApplication(scanBasePackages = "com.baomidou.dynamic.datasource.common")
class AutoConfigurationApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoConfigurationApplication.class, args);
    }
}
