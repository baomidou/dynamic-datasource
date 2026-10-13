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

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.sql.Savepoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SavePointHolder} 单元测试：以 {@code DYNAMIC_<counter>} 命名递增地创建保存点，
 * 释放/回滚时逐个 {@code pollLast} 并委托给被包装的连接，栈空后返回 {@code true}。
 *
 * @author TaoYu
 */
class SavePointHolderTest {

    private Savepoint addOneSavepoint(SavePointHolder holder, ConnectionProxy proxy) throws SQLException {
        Savepoint savepoint = mock(Savepoint.class);
        when(savepoint.getSavepointName()).thenReturn("DYNAMIC_0");
        when(proxy.getSavepointCounter()).thenReturn(0);
        when(proxy.setSavepoint("DYNAMIC_0")).thenReturn(savepoint);
        holder.conversionSavePointHolder();
        return savepoint;
    }

    @Test
    void conversionShouldCreateNamedSavepointAndIncrementCounter() throws SQLException {
        ConnectionProxy proxy = mock(ConnectionProxy.class);
        SavePointHolder holder = new SavePointHolder(proxy);

        addOneSavepoint(holder, proxy);

        verify(proxy).setSavepoint("DYNAMIC_0");
        verify(proxy).setSavepointCounter(1);
        assertThat(holder.getSavePoints()).hasSize(1);
    }

    @Test
    void conversionShouldThrowWhenProxyNull() {
        SavePointHolder holder = new SavePointHolder(null);

        assertThatThrownBy(holder::conversionSavePointHolder).isInstanceOf(SQLTransientConnectionException.class);
    }

    @Test
    void releaseShouldPollLastDelegateAndReportEmpty() throws SQLException {
        ConnectionProxy proxy = mock(ConnectionProxy.class);
        SavePointHolder holder = new SavePointHolder(proxy);
        Savepoint savepoint = addOneSavepoint(holder, proxy);

        boolean empty = holder.releaseSavepoint();

        verify(proxy).releaseSavepoint(savepoint);
        assertThat(empty).isTrue();
        assertThat(holder.getSavePoints()).isEmpty();
    }

    @Test
    void rollbackShouldPollLastDelegateAndReportEmpty() throws SQLException {
        ConnectionProxy proxy = mock(ConnectionProxy.class);
        SavePointHolder holder = new SavePointHolder(proxy);
        Savepoint savepoint = addOneSavepoint(holder, proxy);

        boolean empty = holder.rollbackSavePoint();

        verify(proxy).rollback(savepoint);
        assertThat(empty).isTrue();
        assertThat(holder.getSavePoints()).isEmpty();
    }

    @Test
    void releaseOnEmptyStackShouldReportTrueWithoutDelegating() throws SQLException {
        ConnectionProxy proxy = mock(ConnectionProxy.class);
        SavePointHolder holder = new SavePointHolder(proxy);

        assertThat(holder.releaseSavepoint()).isTrue();
        assertThat(holder.rollbackSavePoint()).isTrue();
    }

    @Test
    void releaseShouldKeepNonEmptyStack() throws SQLException {
        ConnectionProxy proxy = mock(ConnectionProxy.class);
        SavePointHolder holder = new SavePointHolder(proxy);
        // 压入两个保存点，释放最后一个后栈仍剩一个，返回 false
        Savepoint first = mock(Savepoint.class);
        Savepoint second = mock(Savepoint.class);
        when(first.getSavepointName()).thenReturn("DYNAMIC_0");
        when(second.getSavepointName()).thenReturn("DYNAMIC_1");
        when(proxy.getSavepointCounter()).thenReturn(0, 1);
        when(proxy.setSavepoint("DYNAMIC_0")).thenReturn(first);
        when(proxy.setSavepoint("DYNAMIC_1")).thenReturn(second);
        holder.conversionSavePointHolder();
        holder.conversionSavePointHolder();

        boolean empty = holder.releaseSavepoint();

        assertThat(empty).isFalse();
        assertThat(holder.getSavePoints()).hasSize(1);
        assertThat(holder.getConnectionProxy()).isSameAs(proxy);
    }
}
