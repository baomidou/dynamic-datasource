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
import com.baomidou.dynamic.datasource.exception.CannotFindDataSourceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 启动时还没有主库的共享场景：primary 取默认的 master，配置里却只有 db1，主库要到运行期才加进来（#816）。
 * <p>
 * Boot3/4 的 actuator 在启动时创建 db 健康检查，先调用 {@code isWrapperFor} 判断数据源是不是 Spring 的
 * {@link AbstractRoutingDataSource}，并且只捕获 {@link SQLException}。主库缺失时这里曾漏出
 * {@link CannotFindDataSourceException}，应用直接启动失败。Boot2 不做这个判断，一直只打印缺少主库的告警。
 * <p>
 * 版本无关：Boot2/3/4 的 starter 各以薄子类继承本类，跑同一套断言。
 */
@SpringBootTest(classes = AutoConfigurationApplication.class, properties = {
        "spring.datasource.dynamic.datasource.db1.url=jdbc:h2:mem:missing_primary_db1",
        "spring.datasource.dynamic.datasource.db1.username=sa",
        "spring.datasource.dynamic.datasource.db1.driver-class-name=org.h2.Driver"
})
public abstract class AbstractMissingPrimaryTest {

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    DataSource dataSource;

    @Autowired
    DefaultDataSourceCreator dataSourceCreator;

    /**
     * 上下文能启动还不够，db 健康检查也得确实建出来，否则这条用例根本没走到出问题的调用。
     * 主库在同一个用例里补上：补上之前 Wrapper 方法抛 {@link SQLException}，补上之后空 key 的连接和 Wrapper 方法都落到它上面。
     */
    @Test
    void contextShouldStartWithoutPrimaryAndUseThePrimaryAddedLater() throws SQLException {
        DynamicRoutingDataSource routing = (DynamicRoutingDataSource) dataSource;
        assertThat(applicationContext.containsBean("dbHealthContributor")).isTrue();
        assertThat(routing.getPrimary()).isEqualTo("master");
        assertThat(routing.getDataSources()).containsOnlyKeys("db1");
        assertThatThrownBy(() -> routing.isWrapperFor(AbstractRoutingDataSource.class))
                .isInstanceOf(SQLException.class)
                .hasCauseInstanceOf(CannotFindDataSourceException.class);

        routing.addDataSource("master", dataSourceCreator.createDataSource(h2("missing_primary_master")));

        try (Connection connection = routing.getConnection()) {
            assertThat(connection.getMetaData().getURL()).isEqualTo("jdbc:h2:mem:missing_primary_master");
        }
        assertThat(routing.isWrapperFor(AbstractRoutingDataSource.class)).isFalse();
    }

    private DataSourceProperty h2(String name) {
        DataSourceProperty property = new DataSourceProperty();
        property.setPoolName(name);
        property.setUrl("jdbc:h2:mem:" + name);
        property.setUsername("sa");
        property.setPassword("");
        property.setDriverClassName("org.h2.Driver");
        return property;
    }
}
