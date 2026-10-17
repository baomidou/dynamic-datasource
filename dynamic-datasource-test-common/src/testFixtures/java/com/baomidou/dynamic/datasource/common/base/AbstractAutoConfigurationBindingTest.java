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
import com.baomidou.dynamic.datasource.creator.DatasourceInitProperties;
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 自动装配属性绑定的共享版本兼容场景：用同一套 {@code spring.datasource.dynamic.*} 配置（混合 kebab 与
 * camel 松散绑定写法）分别在 Boot2/3/4 上装配，断言 primary / strict / strategy / publicKey、嵌套
 * datasource 分组以及单个数据源下的 init / 连接池嵌套块都能一致地绑定并真正作用到 {@link DynamicRoutingDataSource}。
 * <p>
 * 版本无关：三个 starter 各以薄子类继承本类，从而验证 {@code @ConfigurationProperties} 绑定契约在不同
 * Spring Boot 大版本上的兼容性。本 fixtures 源集以 JDK8 toolchain 编译，故不依赖任何高版本语法。
 * <p>
 * Boot3/4 的 nativeTest 跑同一套断言。Boot 的 AOT 只为内部类或标注了 {@code @NestedConfigurationProperty}
 * 的字段类型生成绑定 hints：连接池配置类型借 {@link DynamicDataSourceProperties} 上同类型的全局字段拿到 hints，
 * {@link DatasourceInitProperties} 则只能靠 dynamic-datasource-native-metadata 补齐，缺失时 init 块在原生镜像下
 * 被静默跳过（#809）。
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
                "spring.datasource.dynamic.datasource.slave_1.init.schema=classpath:db/auto-configuration-binding-schema.sql",
                "spring.datasource.dynamic.datasource.slave_1.init.data=classpath:db/auto-configuration-binding-data.sql",
                "spring.datasource.dynamic.datasource.slave_1.init.continueOnError=false",
                "spring.datasource.dynamic.datasource.slave_1.init.separator=@@",
                "spring.datasource.dynamic.datasource.slave_1.druid.maxActive=5",
                "spring.datasource.dynamic.datasource.slave_1.hikari.maxPoolSize=5",
                "spring.datasource.dynamic.datasource.slave_1.beecp.maxActive=5",
                "spring.datasource.dynamic.datasource.slave_1.dbcp2.maxTotal=5",
                "spring.datasource.dynamic.datasource.slave_1.atomikos.maxPoolSize=5",
                "spring.datasource.dynamic.datasource.slave_1.c3p0.maxPoolSize=5",
                "spring.datasource.dynamic.datasource.slave_1.oracleUcp.maxPoolSize=5",
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

    /**
     * init 的四项都取非默认值，才能区分“已绑定”与“保留了默认值”。
     */
    @Test
    void datasourceInitBlockShouldBind() {
        DatasourceInitProperties init = properties.getDatasource().get("slave_1").getInit();
        assertThat(init.getSchema()).isEqualTo("classpath:db/auto-configuration-binding-schema.sql");
        assertThat(init.getData()).isEqualTo("classpath:db/auto-configuration-binding-data.sql");
        assertThat(init.isContinueOnError()).isFalse();
        assertThat(init.getSeparator()).isEqualTo("@@");
    }

    /**
     * init 块没绑定上时启动不报任何错，脚本只是从未执行，直到这里查询才发现表不存在。
     */
    @Test
    void datasourceInitScriptsShouldRun() throws SQLException {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        try (Connection connection = ds.getDataSource("slave_1").getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT tag FROM init_probe WHERE id = 1")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString("tag")).isEqualTo("init-data");
        }
    }

    /**
     * {@link DataSourceProperty} 上的 7 个连接池嵌套块各绑一项。
     */
    @Test
    void datasourcePoolBlocksShouldBind() {
        DataSourceProperty slave1 = properties.getDatasource().get("slave_1");
        assertThat(slave1.getDruid().getMaxActive()).isEqualTo(5);
        assertThat(slave1.getHikari().getMaxPoolSize()).isEqualTo(5);
        assertThat(slave1.getBeecp().getMaxActive()).isEqualTo(5);
        assertThat(slave1.getDbcp2().getMaxTotal()).isEqualTo(5);
        assertThat(slave1.getAtomikos().getMaxPoolSize()).isEqualTo(5);
        assertThat(slave1.getC3p0().getMaxPoolSize()).isEqualTo(5);
        assertThat(slave1.getOracleUcp().getMaxPoolSize()).isEqualTo(5);
    }

    /**
     * 路由里只能有这里配置的三个库，空 key 回落到的 master 也必须连到配置的 {@code ac_master}。别的场景的启动类曾被组件扫描
     * 带进本上下文，其 JDBC provider 加载的同名 master 顶替了这里的配置（#812）。
     */
    @Test
    void routingDataSourceShouldApplyPrimaryAndStrict() throws SQLException {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        assertThat(ds.getDataSources()).containsOnlyKeys("master", "slave_1", "slave_2");
        // primary 生效：空 key 回落到 master
        try (Connection connection = ds.getDataSource(null).getConnection()) {
            assertThat(connection.getMetaData().getURL()).isEqualTo("jdbc:h2:mem:ac_master");
        }
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

/**
 * 扫描范围不含各场景启动类所在的包，否则别的场景的启动类连同其 bean 会混进本场景的上下文（#812）。
 */
@SpringBootApplication(scanBasePackages = "com.baomidou.dynamic.datasource.common.service")
class AutoConfigurationApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoConfigurationApplication.class, args);
    }
}
