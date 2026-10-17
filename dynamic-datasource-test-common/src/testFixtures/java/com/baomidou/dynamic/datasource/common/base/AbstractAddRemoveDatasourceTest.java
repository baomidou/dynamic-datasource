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
import com.baomidou.dynamic.datasource.common.lifecycle.LifecycleProxyDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariCpConfig;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * 运行时动态增删数据源的共享集成场景（含优雅销毁 graceDestroy 开/关两条路径）。
 * <p>
 * 版本无关：Boot2/3/4 的 starter 各以薄子类继承本类，跑同一套断言，只有加载的
 * Spring Boot 自动装配不同，从而真正验证多版本兼容性，避免逐份复制产生漂移。
 * <p>
 * 配置 master 是为了让上下文带着主库启动：Boot3/4 的 actuator 在启动时创建 db 健康检查，调用的 {@code isWrapperFor}
 * 要先解析出主库，解析不到就启动失败。
 *
 * @author TaoYu
 */
@SpringBootTest(classes = AddRemoveDatasourceApplication.class, webEnvironment = RANDOM_PORT, properties = {
        "spring.datasource.dynamic.datasource.master.url=jdbc:h2:mem:add_remove_master",
        "spring.datasource.dynamic.datasource.master.username=sa",
        "spring.datasource.dynamic.datasource.master.driver-class-name=org.h2.Driver"
})
public abstract class AbstractAddRemoveDatasourceTest {

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

    @ParameterizedTest(name = "{displayName} [customProxy={0}]")
    @ValueSource(booleans = {false, true})
    void removalShouldCloseTheUnderlyingPool(boolean customProxy) throws Exception {
        DynamicRoutingDataSource routing = (DynamicRoutingDataSource) dataSource;
        String name = "remove_" + UUID.randomUUID();
        ItemDataSource item = createHikariDataSource(name);
        DataSource pool = item.getRealDataSource();
        try {
            routing.setGraceDestroy(false);
            routing.addDataSource(name, lifecycleDataSource(item, customProxy));
            assertThat(isPoolClosed(pool)).isFalse();

            routing.removeDataSource(name);

            assertThat(routing.getDataSources()).doesNotContainKey(name);
            assertThat(isPoolClosed(pool)).isTrue();
        } finally {
            removeIfPresent(routing, name);
            closePool(pool);
        }
    }

    @ParameterizedTest(name = "{displayName} [customProxy={0}]")
    @ValueSource(booleans = {false, true})
    void replacementShouldCloseOnlyTheOldUnderlyingPool(boolean customProxy) throws Exception {
        DynamicRoutingDataSource routing = (DynamicRoutingDataSource) dataSource;
        String name = "replace_" + UUID.randomUUID();
        ItemDataSource oldItem = createHikariDataSource(name + "-old");
        ItemDataSource newItem = createHikariDataSource(name + "-new");
        DataSource oldPool = oldItem.getRealDataSource();
        DataSource newPool = newItem.getRealDataSource();
        DataSource replacement = lifecycleDataSource(newItem, customProxy);
        try {
            routing.setGraceDestroy(false);
            routing.addDataSource(name, lifecycleDataSource(oldItem, customProxy));

            routing.addDataSource(name, replacement);

            assertThat(routing.getDataSources().get(name)).isSameAs(replacement);
            assertThat(isPoolClosed(oldPool)).isTrue();
            assertThat(isPoolClosed(newPool)).isFalse();
            routing.removeDataSource(name);
            assertThat(isPoolClosed(newPool)).isTrue();
        } finally {
            removeIfPresent(routing, name);
            closePool(oldPool);
            closePool(newPool);
        }
    }

    @ParameterizedTest(name = "{displayName} [customProxy={0}]")
    @ValueSource(booleans = {false, true})
    void destroyShouldCloseUnderlyingPoolsImmediatelyEvenWhenGraceDestroyIsEnabled(boolean customProxy)
            throws Exception {
        DynamicRoutingDataSource routing = new DynamicRoutingDataSource(Collections.emptyList());
        ItemDataSource item = createHikariDataSource("destroy-" + UUID.randomUUID());
        DataSource pool = item.getRealDataSource();
        try (Connection connection = pool.getConnection()) {
            routing.setGraceDestroy(true);
            routing.addDataSource("master", lifecycleDataSource(item, customProxy));
            assertConnectionUsable(connection);

            routing.destroy();

            assertThat(isPoolClosed(pool)).isTrue();
        } finally {
            closePool(pool);
        }
    }

    @ParameterizedTest(name = "{displayName} [customProxy={0}]")
    @ValueSource(booleans = {false, true})
    void gracefulRemovalShouldWaitForTheUnderlyingPoolsActiveConnection(boolean customProxy) throws Exception {
        DynamicRoutingDataSource routing = (DynamicRoutingDataSource) dataSource;
        String name = "gracefulremove_" + UUID.randomUUID();
        ItemDataSource item = createHikariDataSource(name);
        DataSource pool = item.getRealDataSource();
        try {
            routing.setGraceDestroy(true);
            routing.addDataSource(name, lifecycleDataSource(item, customProxy));
            try (Connection connection = pool.getConnection()) {
                routing.removeDataSource(name);

                assertThat(routing.getDataSources()).doesNotContainKey(name);
                assertPoolRemainsOpenWhileConnectionIsActive(pool, connection);
            }
            awaitPoolClosed(pool);
        } finally {
            routing.setGraceDestroy(false);
            removeIfPresent(routing, name);
            closePool(pool);
        }
    }

    @ParameterizedTest(name = "{displayName} [customProxy={0}]")
    @ValueSource(booleans = {false, true})
    void gracefulReplacementShouldKeepTheOldConnectionAndNewPoolUsable(boolean customProxy) throws Exception {
        DynamicRoutingDataSource routing = (DynamicRoutingDataSource) dataSource;
        String name = "gracefulreplace_" + UUID.randomUUID();
        ItemDataSource oldItem = createHikariDataSource(name + "-old");
        ItemDataSource newItem = createHikariDataSource(name + "-new");
        DataSource oldPool = oldItem.getRealDataSource();
        DataSource newPool = newItem.getRealDataSource();
        DataSource replacement = lifecycleDataSource(newItem, customProxy);
        try {
            routing.setGraceDestroy(true);
            routing.addDataSource(name, lifecycleDataSource(oldItem, customProxy));
            try (Connection connection = oldPool.getConnection()) {
                routing.addDataSource(name, replacement);

                assertThat(routing.getDataSources().get(name)).isSameAs(replacement);
                assertPoolRemainsOpenWhileConnectionIsActive(oldPool, connection);
                assertThat(isPoolClosed(newPool)).isFalse();
            }
            awaitPoolClosed(oldPool);
            assertThat(isPoolClosed(newPool)).isFalse();
        } finally {
            routing.setGraceDestroy(false);
            removeIfPresent(routing, name);
            closePool(oldPool);
            closePool(newPool);
        }
    }

    private ItemDataSource createHikariDataSource(String name) throws ClassNotFoundException {
        HikariCpConfig hikari = new HikariCpConfig();
        hikari.setMaximumPoolSize(2);
        hikari.setMinimumIdle(0);
        hikari.setConnectionTimeout(1000L);
        DataSourceProperty property = new DataSourceProperty();
        property.setHikari(hikari);
        property.setPoolName(name);
        property.setType(Class.forName("com.zaxxer.hikari.HikariDataSource").asSubclass(DataSource.class));
        property.setUrl("jdbc:h2:mem:" + name);
        property.setUsername("sa");
        property.setPassword("");
        property.setDriverClassName("org.h2.Driver");
        DataSource created = dataSourceCreator.createDataSource(property);
        assertThat(created).isInstanceOf(ItemDataSource.class);
        ItemDataSource item = (ItemDataSource) created;
        assertThat(item.getRealDataSource().getClass().getName()).isEqualTo("com.zaxxer.hikari.HikariDataSource");
        return item;
    }

    private DataSource lifecycleDataSource(ItemDataSource item, boolean customProxy) {
        if (customProxy) {
            // This non-closing proxy can only expose the pool through the test SPI provider.
            return new LifecycleProxyDataSource(item.getRealDataSource());
        }
        return item;
    }

    private void assertPoolRemainsOpenWhileConnectionIsActive(DataSource pool, Connection connection)
            throws Exception {
        // Observe more than one destroyer poll, so an immediate-close regression cannot pass by racing.
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(300);
        do {
            assertThat(isPoolClosed(pool)).isFalse();
            assertConnectionUsable(connection);
            Thread.sleep(25L);
        } while (System.nanoTime() < deadline);
    }

    private void assertConnectionUsable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("select 1")) {
            assertThat(result.next()).isTrue();
            assertThat(result.getInt(1)).isEqualTo(1);
        }
    }

    private void awaitPoolClosed(DataSource pool) throws Exception {
        // Unwrapped Hikari pools close as soon as their active connection is returned.
        // A missing detector otherwise waits ten seconds, which must not satisfy this assertion.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (isPoolClosed(pool)) {
                return;
            }
            Thread.sleep(25L);
        }
        assertThat(isPoolClosed(pool)).as("underlying Hikari pool should close after its active connection returns")
                .isTrue();
    }

    private boolean isPoolClosed(DataSource pool) throws SQLException {
        // Probe through JDBC instead of adding a Hikari compile dependency or native reflection metadata.
        try (Connection ignored = pool.getConnection()) {
            return false;
        } catch (SQLException exception) {
            if (exception.getMessage() != null && exception.getMessage().contains("has been closed")) {
                return true;
            }
            throw exception;
        }
    }

    private void removeIfPresent(DynamicRoutingDataSource routing, String name) {
        if (routing.getDataSources().containsKey(name)) {
            routing.removeDataSource(name);
        }
    }

    private void closePool(DataSource pool) throws Exception {
        ((AutoCloseable) pool).close();
    }
}

/**
 * 扫描范围不含各场景启动类所在的包，否则别的场景的启动类连同其 bean 会混进本场景的上下文（#812）。
 */
@SpringBootApplication(scanBasePackages = "com.baomidou.dynamic.datasource.common.service")
class AddRemoveDatasourceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AddRemoveDatasourceApplication.class, args);
    }
}
