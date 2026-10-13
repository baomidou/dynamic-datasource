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
import org.springframework.transaction.CannotCreateTransactionException;

import java.sql.Connection;
import java.sql.Savepoint;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ConnectionFactory} 单元测试：基于 ThreadLocal 的连接登记/查询、扁平事务的 {@code notify}
 * 提交回滚、以及嵌套事务保存点的创建与释放。每个用例使用独立 xid 避免线程上下文相互污染。
 *
 * @author TaoYu
 */
class ConnectionFactoryTest {

    @Test
    void putThenGetShouldRoundTrip() throws Exception {
        String xid = "x-roundtrip";
        Connection connection = mock(Connection.class);
        ConnectionProxy proxy = new ConnectionProxy(connection, "ds1");

        ConnectionFactory.putConnection(xid, "ds1", proxy);

        assertThat(ConnectionFactory.getConnection(xid, "ds1")).isSameAs(proxy);
        assertThat(ConnectionFactory.getConnection(xid, "missingDs")).isNull();
        assertThat(ConnectionFactory.getConnection("missingXid", "ds1")).isNull();
        // putConnection 会强制关闭自动提交
        verify(connection).setAutoCommit(false);
    }

    @Test
    void putShouldNotOverwriteExistingDs() throws Exception {
        String xid = "x-overwrite";
        ConnectionProxy first = new ConnectionProxy(mock(Connection.class), "ds");
        ConnectionProxy second = new ConnectionProxy(mock(Connection.class), "ds");

        ConnectionFactory.putConnection(xid, "ds", first);
        ConnectionFactory.putConnection(xid, "ds", second);

        assertThat(ConnectionFactory.getConnection(xid, "ds")).isSameAs(first);
    }

    @Test
    void notifyFlatCommitShouldCommitAndRemove() throws Exception {
        String xid = "x-flat-commit";
        Connection connection = mock(Connection.class);
        ConnectionFactory.putConnection(xid, "ds", new ConnectionProxy(connection, "ds"));

        ConnectionFactory.notify(xid, true);

        verify(connection).commit();
        verify(connection).close();
        assertThat(ConnectionFactory.getConnection(xid, "ds")).isNull();
    }

    @Test
    void notifyFlatRollbackShouldRollbackAndRemove() throws Exception {
        String xid = "x-flat-rollback";
        Connection connection = mock(Connection.class);
        ConnectionFactory.putConnection(xid, "ds", new ConnectionProxy(connection, "ds"));

        ConnectionFactory.notify(xid, false);

        verify(connection).rollback();
        verify(connection, never()).commit();
        verify(connection).close();
    }

    @Test
    void notifyUnknownXidShouldBeNoOp() throws Exception {
        // 未登记过的 xid 不应抛异常，也不影响其他连接
        ConnectionFactory.notify("x-unknown-never-used", true);
    }

    @Test
    void createSavepointShouldRegisterNestedHolder() throws Exception {
        String xid = "x-create-savepoint";
        Connection connection = mock(Connection.class);
        when(connection.setSavepoint(anyString())).thenReturn(mock(Savepoint.class));
        ConnectionFactory.putConnection(xid, "ds", new ConnectionProxy(connection, "ds"));

        assertThat(ConnectionFactory.hasSavepoint(xid)).isFalse();
        ConnectionFactory.createSavepoint(xid);

        assertThat(ConnectionFactory.hasSavepoint(xid)).isTrue();
        verify(connection).setSavepoint("DYNAMIC_0");
    }

    @Test
    void notifyWithSavepointShouldReleaseNested() throws Exception {
        String xid = "x-nested-release";
        Connection connection = mock(Connection.class);
        Savepoint savepoint = mock(Savepoint.class);
        when(savepoint.getSavepointName()).thenReturn("DYNAMIC_0");
        when(connection.setSavepoint(anyString())).thenReturn(savepoint);
        ConnectionFactory.putConnection(xid, "ds", new ConnectionProxy(connection, "ds"));
        ConnectionFactory.createSavepoint(xid);

        ConnectionFactory.notify(xid, true);

        // 嵌套事务提交走保存点释放路径，而非直接提交底层连接
        verify(connection).releaseSavepoint(savepoint);
        verify(connection, never()).commit();
    }

    @Test
    void putConnectionShouldRefuseEnlistmentWhenAutoCommitCannotBeDisabled() throws Exception {
        // 驱动拒绝 setAutoCommit(false) 时绝不能继续登记：否则该连接上每条 SQL 立即落库，
        // 而事务机制还以为自己能回滚，产生"部分成功"脏数据。
        String xid = "x-autocommit-refused";
        Connection connection = mock(Connection.class);
        doThrow(new SQLException("driver refuses")).when(connection).setAutoCommit(false);

        assertThatThrownBy(() -> ConnectionFactory.putConnection(xid, "ds",
                new ConnectionProxy(connection, "ds")))
                .isInstanceOf(CannotCreateTransactionException.class)
                .hasMessageContaining("ds");
        assertThat(ConnectionFactory.getConnection(xid, "ds")).isNull();
    }

    @Test
    void notifyWithSavepointRollbackShouldRollbackAndCloseUnmarkedConnection() throws Exception {
        // 场景：外层先建保存点（ds1 被标记），嵌套内层又新开了一个数据源（ds2 无保存点）后回滚。
        // ds2 的 proxy 一旦脱离 connectionProxyMap 就再也拿不到 notify()，
        // 旧代码调的是 no-op 的 proxy.rollback()，物理连接既不回滚也不归还，永久泄漏。
        String xid = "x-nested-rollback-unmarked";
        Connection markedConn = mock(Connection.class);
        Savepoint savepoint = mock(Savepoint.class);
        when(savepoint.getSavepointName()).thenReturn("DYNAMIC_0");
        when(markedConn.setSavepoint(anyString())).thenReturn(savepoint);
        ConnectionFactory.putConnection(xid, "ds1", new ConnectionProxy(markedConn, "ds1"));
        ConnectionFactory.createSavepoint(xid);
        Connection unmarkedConn = mock(Connection.class);
        ConnectionFactory.putConnection(xid, "ds2", new ConnectionProxy(unmarkedConn, "ds2"));

        ConnectionFactory.notify(xid, false);

        // 被标记连接回滚到保存点，仍留在事务里等最外层结算
        verify(markedConn).rollback(savepoint);
        verify(markedConn, never()).close();
        // 未标记连接必须真实回滚并归还连接池
        verify(unmarkedConn).rollback();
        verify(unmarkedConn).close();
        assertThat(ConnectionFactory.getConnection(xid, "ds2")).isNull();
    }

    @Test
    void createSavepointWithoutAnyConnectionShouldBeNoOp() {
        // 外层事务全程没取过连接，内层 NESTED 进入时 xid 下没有连接映射：
        // 旧代码在这里直接 .values() 抛 NPE，应与 notify() 一样安全返回。
        String xid = "x-savepoint-no-connection";
        ConnectionFactory.createSavepoint(xid);
        assertThat(ConnectionFactory.hasSavepoint(xid)).isFalse();
    }

    @Test
    void hasSavepointShouldBeFalseForUnknownXid() {
        assertThat(ConnectionFactory.hasSavepoint("x-no-savepoint")).isFalse();
    }
}
