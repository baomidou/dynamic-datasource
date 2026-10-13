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
package com.baomidou.dynamic.datasource.ds;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * {@link ItemDataSource} 单元测试：连接委托给内层 dataSource、Wrapper 判定覆盖真实/包装两层、
 * unwrap 逐级匹配、以及 {@code close()} 通过反射调用 realDataSource 的无参 close。
 *
 * @author TaoYu
 */
class ItemDataSourceTest {

    /** 带公共 close() 的具体数据源，用于验证 ItemDataSource.close() 的反射调用路径。 */
    static class ClosableDataSource extends AbstractDataSource {
        boolean closed = false;

        @Override
        public Connection getConnection() {
            return mock(Connection.class);
        }

        @Override
        public Connection getConnection(String username, String password) {
            return mock(Connection.class);
        }

        public void close() {
            closed = true;
        }
    }

    private ItemDataSource newItem() {
        ClosableDataSource real = new ClosableDataSource();
        DataSource wrapper = mock(DataSource.class);
        return new ItemDataSource("item", real, wrapper, false, false, null);
    }

    @Test
    void gettersShouldExposeConstructorArgs() {
        ItemDataSource item = newItem();
        assertThat(item.getName()).isEqualTo("item");
        assertThat(item.getRealDataSource()).isInstanceOf(ClosableDataSource.class);
        assertThat(item.getP6spy()).isFalse();
        assertThat(item.getSeata()).isFalse();
    }

    @Test
    void getConnectionShouldDelegateToWrapper() throws Exception {
        DataSource wrapper = mock(DataSource.class);
        Connection conn = mock(Connection.class);
        org.mockito.Mockito.when(wrapper.getConnection()).thenReturn(conn);
        org.mockito.Mockito.when(wrapper.getConnection("sa", "")).thenReturn(conn);
        ItemDataSource item = new ItemDataSource("item", new ClosableDataSource(), wrapper, false, false, null);

        assertThat(item.getConnection()).isSameAs(conn);
        assertThat(item.getConnection("sa", "")).isSameAs(conn);
    }

    @Test
    void isWrapperForShouldCoverSelfRealAndWrapper() throws Exception {
        ItemDataSource item = newItem();
        assertThat(item.isWrapperFor(ItemDataSource.class)).isTrue();
        assertThat(item.isWrapperFor(ClosableDataSource.class)).isTrue();
        assertThat(item.isWrapperFor(DataSource.class)).isTrue();
        assertThat(item.isWrapperFor(CharSequence.class)).isFalse();
    }

    @Test
    void unwrapShouldMatchInOrder() throws Exception {
        ItemDataSource item = newItem();
        assertThat(item.unwrap(ItemDataSource.class)).isSameAs(item);
        assertThat(item.unwrap(ClosableDataSource.class)).isSameAs(item.getRealDataSource());
        assertThat(item.unwrap(CharSequence.class)).isNull();
    }

    @Test
    void closeShouldInvokeRealDataSourceCloseReflectively() {
        ClosableDataSource real = new ClosableDataSource();
        ItemDataSource item = new ItemDataSource("item", real, mock(DataSource.class), false, false, null);

        item.close();

        assertThat(real.closed).isTrue();
    }
}
