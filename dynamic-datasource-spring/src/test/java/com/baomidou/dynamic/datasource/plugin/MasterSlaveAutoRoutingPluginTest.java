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
package com.baomidou.dynamic.datasource.plugin;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link MasterSlaveAutoRoutingPlugin} 读写自动路由的单元测试：SELECT 走从库、写操作走主库，
 * 且执行完毕后清理线程上下文（不泄漏）。
 * <p>
 * 通过 mock 的 {@code Invocation.proceed()} 在切面 push 之后、poll 之前捕获当前数据源 key，
 * 从而断言插件实际切换到的目标库。
 *
 * @author TaoYu
 */
class MasterSlaveAutoRoutingPluginTest {

    private final MasterSlaveAutoRoutingPlugin plugin = new MasterSlaveAutoRoutingPlugin();

    @AfterEach
    void tearDown() {
        DynamicDataSourceContextHolder.clear();
    }

    private Object interceptWith(SqlCommandType commandType) throws Throwable {
        // MappedStatement 是 final 类，Mockito 无法 mock；直接构造真实对象（也更贴近实际）。
        Configuration configuration = new Configuration();
        ResultMap resultMap = new ResultMap.Builder(configuration, "probe-rm", Object.class,
                Collections.emptyList()).build();
        MappedStatement ms = new MappedStatement.Builder(configuration, "probe",
                new StaticSqlSource(configuration, "/* probe */"), commandType)
                .resultMaps(Collections.singletonList(resultMap))
                .build();
        Invocation invocation = mock(Invocation.class);
        when(invocation.getArgs()).thenReturn(new Object[]{ms, new Object()});
        // proceed 发生在 push 之后：此刻 peek 即为插件切换到的数据源
        when(invocation.proceed()).thenAnswer(inv -> DynamicDataSourceContextHolder.peek());
        Object routed = plugin.intercept(invocation);
        // 栈应已 poll 回空
        assertThat(DynamicDataSourceContextHolder.peek()).isNull();
        return routed;
    }

    @Test
    void selectShouldRouteToSlave() throws Throwable {
        assertThat(interceptWith(SqlCommandType.SELECT)).isEqualTo("slave");
    }

    @Test
    void insertShouldRouteToMaster() throws Throwable {
        assertThat(interceptWith(SqlCommandType.INSERT)).isEqualTo("master");
    }

    @Test
    void updateShouldRouteToMaster() throws Throwable {
        assertThat(interceptWith(SqlCommandType.UPDATE)).isEqualTo("master");
    }

    @Test
    void deleteShouldRouteToMaster() throws Throwable {
        assertThat(interceptWith(SqlCommandType.DELETE)).isEqualTo("master");
    }
}
