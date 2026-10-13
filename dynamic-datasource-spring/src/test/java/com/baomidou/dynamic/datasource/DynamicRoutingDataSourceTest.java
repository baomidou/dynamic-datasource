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
package com.baomidou.dynamic.datasource;

import com.baomidou.dynamic.datasource.exception.CannotFindDataSourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * {@link DynamicRoutingDataSource} 路由决策的单元测试，覆盖以下基本功能分支：
 * 空 key 回退主库、具名命中、分组路由（下划线命名）、strict 模式异常、以及删除校验。
 * <p>
 * 纯单元：不依赖 Spring 容器与真实连接池，用 mock 数据源即可确定性地验证 {@code getDataSource}
 * 的分派逻辑，补齐此前最薄弱的分支覆盖。
 *
 * @author TaoYu
 */
class DynamicRoutingDataSourceTest {

    private DataSource master;
    private DataSource g1;
    private DataSource g2;
    private DynamicRoutingDataSource router;

    @BeforeEach
    void setUp() {
        master = mock(DataSource.class);
        g1 = mock(DataSource.class);
        g2 = mock(DataSource.class);
        router = new DynamicRoutingDataSource(Collections.emptyList());
        router.setPrimary("master");
        router.addDataSource("master", master);
        // 下划线命名自动归入分组 "g"
        router.addDataSource("g_1", g1);
        router.addDataSource("g_2", g2);
    }

    @Test
    void emptyKeyShouldRouteToPrimary() {
        assertThat(router.getDataSource(null)).isSameAs(master);
        assertThat(router.getDataSource("")).isSameAs(master);
        assertThat(router.getDataSource("  ")).isSameAs(master);
    }

    @Test
    void namedDataSourceShouldWinOverPrimary() {
        DataSource db1 = mock(DataSource.class);
        router.addDataSource("db1", db1);
        assertThat(router.getDataSource("db1")).isSameAs(db1);
    }

    @Test
    void unknownKeyNonStrictFallsBackToPrimary() {
        router.setStrict(false);
        assertThat(router.getDataSource("no-such-ds")).isSameAs(master);
    }

    @Test
    void unknownKeyStrictThrows() {
        router.setStrict(true);
        assertThatThrownBy(() -> router.getDataSource("no-such-ds"))
                .isInstanceOf(CannotFindDataSourceException.class)
                .hasMessageContaining("no-such-ds");
    }

    @Test
    void groupRoutingShouldPickFromGroupMembers() {
        assertThat(router.getGroupDataSources()).containsKey("g");
        assertThat(router.getGroupDataSources().get("g").size()).isEqualTo(2);
        // 负载均衡策略在两个成员间轮转，两次调用应覆盖全部成员
        Set<DataSource> picked = new HashSet<>();
        picked.add(router.getDataSource("g"));
        picked.add(router.getDataSource("g"));
        assertThat(picked).containsExactlyInAnyOrder(g1, g2);
    }

    @Test
    void removePrimaryShouldBeRejected() {
        assertThatThrownBy(() -> router.removeDataSource("master"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("primary");
    }

    @Test
    void removeBlankShouldBeRejected() {
        assertThatThrownBy(() -> router.removeDataSource(""))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void removeGroupMemberShouldUpdateMapAndGroup() {
        router.removeDataSource("g_1");
        assertThat(router.getDataSources().keySet()).doesNotContain("g_1");
        assertThat(router.getGroupDataSources().get("g").size()).isEqualTo(1);
    }

    @Test
    void removingLastGroupMemberShouldDropTheGroup() {
        // 旧实现：最后一个成员被删后组还在，后续路由命中空组直接 ArithmeticException(/ by zero)
        router.removeDataSource("g_1");
        router.removeDataSource("g_2");
        assertThat(router.getGroupDataSources()).doesNotContainKey("g");

        router.setStrict(false);
        assertThat(router.getDataSource("g")).isSameAs(master);
        router.setStrict(true);
        assertThatThrownBy(() -> router.getDataSource("g"))
                .isInstanceOf(CannotFindDataSourceException.class);

        // 重新加入同组数据源后，组正常重建
        router.setStrict(false);
        router.addDataSource("g_3", g1);
        assertThat(router.getDataSource("g")).isSameAs(g1);
    }
}
