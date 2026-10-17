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
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link DynamicRoutingDataSource} 路由决策的单元测试，覆盖以下基本功能分支：
 * 空 key 回退主库、具名命中、分组路由（下划线命名）、strict 模式异常、删除校验，以及 JDBC Wrapper 方法的委托与报错。
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

    /**
     * 路由数据源自身没实现所问的类型时，Wrapper 方法委托给当前路由到的数据源。
     */
    @Test
    void wrapperMethodsShouldDelegateToRoutedDataSource() throws SQLException {
        Closeable pool = mock(Closeable.class);
        when(master.isWrapperFor(Closeable.class)).thenReturn(true);
        when(master.unwrap(Closeable.class)).thenReturn(pool);
        assertThat(router.isWrapperFor(Closeable.class)).isTrue();
        assertThat(router.unwrap(Closeable.class)).isSameAs(pool);
    }

    /**
     * 没有主库时 Wrapper 方法按 JDBC 约定抛 {@link SQLException}，原来的 {@link CannotFindDataSourceException} 留作 cause。
     * Boot3/4 的 actuator 在启动时调用 {@code isWrapperFor} 且只捕获 {@link SQLException}，漏出去的
     * {@link CannotFindDataSourceException} 会让应用启动失败（#816）。
     */
    @Test
    void wrapperMethodsShouldReportMissingPrimaryAsSQLException() {
        DynamicRoutingDataSource empty = new DynamicRoutingDataSource(Collections.emptyList());
        assertThatThrownBy(() -> empty.isWrapperFor(Closeable.class))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("primary")
                .hasCauseInstanceOf(CannotFindDataSourceException.class);
        assertThatThrownBy(() -> empty.unwrap(Closeable.class))
                .isInstanceOf(SQLException.class)
                .hasMessageContaining("primary")
                .hasCauseInstanceOf(CannotFindDataSourceException.class);
    }

    /**
     * 所问的类型路由数据源自身就实现了，不用解析数据源，没有主库也照常回答。
     */
    @Test
    void wrapperMethodsShouldAnswerForRouterItselfWithoutPrimary() throws SQLException {
        DynamicRoutingDataSource empty = new DynamicRoutingDataSource(Collections.emptyList());
        assertThat(empty.isWrapperFor(DynamicRoutingDataSource.class)).isTrue();
        assertThat(empty.unwrap(DataSource.class)).isSameAs(empty);
    }

    /**
     * strict 模式下当前指定的数据源不存在，Wrapper 方法同样抛 {@link SQLException}。
     */
    @Test
    void wrapperMethodsShouldReportStrictMissAsSQLException() {
        router.setStrict(true);
        DynamicDataSourceContextHolder.push("no-such-ds");
        try {
            assertThatThrownBy(() -> router.isWrapperFor(Closeable.class))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("no-such-ds")
                    .hasCauseInstanceOf(CannotFindDataSourceException.class);
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }
}
