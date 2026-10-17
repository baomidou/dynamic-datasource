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
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceRefresher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * 配置中心刷新场景的共享集成测试（新增 / 变更 / 移除 / 空配置 / 熔断 / primary 保护 / 手工添加）。
 * <p>
 * 版本无关：Boot2/3/4 的 starter 各以薄子类继承本类，跑同一套断言，只有加载的
 * Spring Boot 自动装配不同，从而真正验证多版本兼容性，避免逐份复制产生漂移。
 * <p>
 * 最关键的一条断言是 {@link #shouldRebuildOnlyChangedDataSource()}：改一个库时其余库的
 * 连接池实例必须还是同一个——这正是「不做 diff 就全量重建」最容易踩坏的地方。
 *
 * @author TaoYu
 */
@SpringBootTest(classes = RefreshDatasourceApplication.class, webEnvironment = RANDOM_PORT, properties = {
        "spring.datasource.dynamic.primary=master",
        "spring.datasource.dynamic.strict=true",
        "spring.datasource.dynamic.datasource.master.url=jdbc:h2:mem:refresh_master;DB_CLOSE_DELAY=-1",
        "spring.datasource.dynamic.datasource.master.username=sa",
        "spring.datasource.dynamic.datasource.master.password=",
        "spring.datasource.dynamic.datasource.master.driver-class-name=org.h2.Driver",
        "spring.datasource.dynamic.datasource.slave_1.url=jdbc:h2:mem:refresh_slave1;DB_CLOSE_DELAY=-1",
        "spring.datasource.dynamic.datasource.slave_1.username=sa",
        "spring.datasource.dynamic.datasource.slave_1.password=",
        "spring.datasource.dynamic.datasource.slave_1.driver-class-name=org.h2.Driver",
        "spring.datasource.dynamic.datasource.slave_2.url=jdbc:h2:mem:refresh_slave2;DB_CLOSE_DELAY=-1",
        "spring.datasource.dynamic.datasource.slave_2.username=sa",
        "spring.datasource.dynamic.datasource.slave_2.password=",
        "spring.datasource.dynamic.datasource.slave_2.driver-class-name=org.h2.Driver"
})
public abstract class AbstractRefreshDatasourceTest {

    private static final String MASTER = "master";
    private static final String SLAVE_1 = "slave_1";
    private static final String SLAVE_2 = "slave_2";
    private static final String MANUAL = "manual_1";

    @Autowired
    DataSource dataSource;

    @Autowired
    DynamicDataSourceRefresher refresher;

    @Autowired
    DefaultDataSourceCreator dataSourceCreator;

    /**
     * 各用例共享同一个 Spring 上下文，刷新器状态会跨用例累积，跑完统一还原成初始的三份配置
     */
    @AfterEach
    void restore() {
        refresher.refresh(initialConfig());
    }

    @Test
    void shouldSkipWhenNothingChanged() {
        DataSource slaveBefore = routing().getDataSources().get(SLAVE_1);

        DynamicDataSourceRefresher.Result result = refresher.refresh(initialConfig());

        assertThat(result.isRejected()).isFalse();
        assertThat(result.isChanged()).isFalse();
        assertThat(result.getSkipped()).contains(MASTER, SLAVE_1, SLAVE_2);
        assertThat(routing().getDataSources().get(SLAVE_1)).isSameAs(slaveBefore);
    }

    @Test
    void shouldRebuildOnlyChangedDataSource() {
        DataSource slaveBefore = routing().getDataSources().get(SLAVE_1);
        DataSource untouchedBefore = routing().getDataSources().get(SLAVE_2);

        Map<String, DataSourceProperty> next = initialConfig();
        // 换库地址：H2 内存库随时可建，能真正走通「重建成功」的分支
        next.get(SLAVE_1).setUrl("jdbc:h2:mem:refresh_slave1_migrated;DB_CLOSE_DELAY=-1");

        DynamicDataSourceRefresher.Result result = refresher.refresh(next);

        assertThat(result.getUpdated()).containsExactly(SLAVE_1);
        assertThat(result.getSkipped()).contains(MASTER, SLAVE_2);
        // 改了的重建，没改的必须还是同一个连接池实例
        assertThat(routing().getDataSources().get(SLAVE_1)).isNotSameAs(slaveBefore);
        assertThat(routing().getDataSources().get(SLAVE_2)).isSameAs(untouchedBefore);
    }

    @Test
    void shouldKeepOldDataSourceWhenCreateFailed() {
        DataSource slaveBefore = routing().getDataSources().get(SLAVE_1);

        Map<String, DataSourceProperty> next = initialConfig();
        // 用一个必然查找不到的 JNDI 名触发创建失败：不同 Boot 版本的连接池建连时机不一致
        // （Boot3 的 Hikari 构造即建连，Boot2 的 hikari-java7 则延迟），只有 JNDI 查找在任何环境下都失败。
        next.get(SLAVE_1).setJndiName("java:comp/env/jdbc/" + UUID.randomUUID());

        DynamicDataSourceRefresher.Result result = refresher.refresh(next);

        assertThat(result.getFailures()).containsKey(SLAVE_1);
        assertThat(result.getUpdated()).doesNotContain(SLAVE_1);
        // 创建失败必须保持旧池可用，且不影响其他库
        assertThat(routing().getDataSources().get(SLAVE_1)).isSameAs(slaveBefore);
        assertThat(routing().getDataSources()).containsKeys(MASTER, SLAVE_2);
    }

    @Test
    void shouldAddAndRemoveDataSource() {
        Map<String, DataSourceProperty> next = initialConfig();
        next.remove(SLAVE_2);
        next.put("slave_3", h2("jdbc:h2:mem:refresh_slave3;DB_CLOSE_DELAY=-1"));

        DynamicDataSourceRefresher.Result result = refresher.refresh(next);

        assertThat(result.getAdded()).containsExactly("slave_3");
        assertThat(result.getRemoved()).containsExactly(SLAVE_2);
        assertThat(routing().getDataSources()).containsKey("slave_3").doesNotContainKey(SLAVE_2);
    }

    @Test
    void shouldRejectEmptyConfig() {
        int before = routing().getDataSources().size();

        DynamicDataSourceRefresher.Result result = refresher.refresh(Collections.<String, DataSourceProperty>emptyMap());

        assertThat(result.isRejected()).isTrue();
        assertThat(routing().getDataSources()).hasSize(before);
    }

    @Test
    void shouldNotRemovePrimary() {
        Map<String, DataSourceProperty> next = initialConfig();
        next.remove(MASTER);

        DynamicDataSourceRefresher.Result result = refresher.refresh(next);

        assertThat(result.getFailures()).containsKey(MASTER);
        assertThat(routing().getDataSources()).containsKey(MASTER);
    }

    @Test
    void shouldRefuseMassRemoval() {
        Map<String, DataSourceProperty> next = initialConfig();
        next.remove(SLAVE_1);
        next.remove(SLAVE_2);

        DynamicDataSourceRefresher.Result result = refresher.refresh(next);

        assertThat(result.isRemovalRejected()).isTrue();
        assertThat(result.getRemoved()).isEmpty();
        assertThat(routing().getDataSources()).containsKeys(SLAVE_1, SLAVE_2);
    }

    @Test
    void shouldKeepManuallyAddedDataSource() {
        DataSourceProperty manual = h2("jdbc:h2:mem:refresh_manual;DB_CLOSE_DELAY=-1");
        manual.setPoolName(MANUAL);
        routing().addDataSource(MANUAL, dataSourceCreator.createDataSource(manual));

        DynamicDataSourceRefresher.Result result = refresher.refresh(initialConfig());

        // 运行期手工加进来的库不在刷新器的托管快照里，不应被当成「配置里没有」而删掉
        assertThat(refresher.getManagedDataSourceNames()).doesNotContain(MANUAL);
        assertThat(result.getRemoved()).isEmpty();
        assertThat(routing().getDataSources()).containsKey(MANUAL);
    }

    private DynamicRoutingDataSource routing() {
        return (DynamicRoutingDataSource) dataSource;
    }

    /**
     * 模拟配置中心下发的一份「与当前一致」的配置
     */
    private static Map<String, DataSourceProperty> initialConfig() {
        Map<String, DataSourceProperty> config = new LinkedHashMap<>();
        config.put(MASTER, h2("jdbc:h2:mem:refresh_master;DB_CLOSE_DELAY=-1"));
        config.put(SLAVE_1, h2("jdbc:h2:mem:refresh_slave1;DB_CLOSE_DELAY=-1"));
        config.put(SLAVE_2, h2("jdbc:h2:mem:refresh_slave2;DB_CLOSE_DELAY=-1"));
        return config;
    }

    private static DataSourceProperty h2(String url) {
        DataSourceProperty property = new DataSourceProperty();
        property.setUrl(url);
        property.setUsername("sa");
        property.setPassword("");
        property.setDriverClassName("org.h2.Driver");
        return property;
    }
}

/**
 * 扫描范围不含各场景启动类所在的包，否则别的场景的启动类连同其 bean 会混进本场景的上下文（#812）。
 */
@SpringBootApplication(scanBasePackages = "com.baomidou.dynamic.datasource.common.service")
class RefreshDatasourceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RefreshDatasourceApplication.class, args);
    }
}
