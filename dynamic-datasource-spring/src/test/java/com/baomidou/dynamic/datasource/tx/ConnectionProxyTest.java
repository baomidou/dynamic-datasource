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
package com.baomidou.dynamic.datasource.tx;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ConnectionProxy} 单元测试：真实的提交/回滚只由 {@code notify(...)} 触发，而
 * {@code commit()/rollback()/close()} 被刻意置为空实现，{@code setAutoCommit} 强制关闭自动提交。
 *
 * @author TaoYu
 */
class ConnectionProxyTest {

    @Test
    void notifyCommitShouldCommitAndCloseUnderlying() throws SQLException {
        Connection connection = mock(Connection.class);
        ConnectionProxy proxy = new ConnectionProxy(connection, "ds");

        proxy.notify(true);

        verify(connection).commit();
        verify(connection, never()).rollback();
        verify(connection).close();
    }

    @Test
    void notifyRollbackShouldRollbackAndCloseUnderlying() throws SQLException {
        Connection connection = mock(Connection.class);
        ConnectionProxy proxy = new ConnectionProxy(connection, "ds");

        proxy.notify(false);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void notifyShouldCloseAndRethrowWhenCommitFails() throws SQLException {
        Connection connection = mock(Connection.class);
        doThrow(new SQLException("boom")).when(connection).commit();
        ConnectionProxy proxy = new ConnectionProxy(connection, "ds");

        assertThatThrownBy(() -> proxy.notify(true)).isInstanceOf(SQLException.class);
        // 提交失败仍要在 finally 里释放连接
        verify(connection).close();
    }

    @Test
    void commitRollbackCloseShouldBeNoOps() throws SQLException {
        Connection connection = mock(Connection.class);
        ConnectionProxy proxy = new ConnectionProxy(connection, "ds");

        proxy.commit();
        proxy.rollback();
        proxy.close();

        verify(connection, never()).commit();
        verify(connection, never()).rollback();
        verify(connection, never()).close();
    }

    @Test
    void setAutoCommitShouldAlwaysDisableOnUnderlying() throws SQLException {
        Connection connection = mock(Connection.class);
        ConnectionProxy proxy = new ConnectionProxy(connection, "ds");

        proxy.setAutoCommit(true);
        proxy.setAutoCommit(false);

        verify(connection, org.mockito.Mockito.times(2)).setAutoCommit(false);
    }

    @Test
    void delegatingMethodsShouldForwardToUnderlying() throws SQLException {
        Connection connection = mock(Connection.class);
        when(connection.nativeSQL("select 1")).thenReturn("/*native*/ select 1");
        when(connection.getAutoCommit()).thenReturn(true);
        ConnectionProxy proxy = new ConnectionProxy(connection, "ds");

        assertThat(proxy.nativeSQL("select 1")).isEqualTo("/*native*/ select 1");
        assertThat(proxy.getAutoCommit()).isTrue();
        verify(connection).nativeSQL("select 1");
    }

    @Test
    void savepointCounterShouldBeReadWrite() {
        ConnectionProxy proxy = new ConnectionProxy(mock(Connection.class), "ds");
        assertThat(proxy.getSavepointCounter()).isZero();
        proxy.setSavepointCounter(7);
        assertThat(proxy.getSavepointCounter()).isEqualTo(7);
    }

    @Test
    void equalsAndHashCodeShouldUseConnectionAndDs() {
        Connection connection = mock(Connection.class);
        ConnectionProxy a = new ConnectionProxy(connection, "ds");
        ConnectionProxy b = new ConnectionProxy(connection, "ds");
        ConnectionProxy c = new ConnectionProxy(connection, "other");

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isEqualTo(a);
        assertThat(a).isNotEqualTo("notAProxy");
    }
}
