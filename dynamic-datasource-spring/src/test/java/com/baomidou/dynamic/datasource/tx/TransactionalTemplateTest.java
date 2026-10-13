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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.TransactionSynchronization;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * {@link TransactionalTemplate} 单元测试：重点钉死"完成链任一步抛错都不许把
 * xid/连接/同步器残留在当前线程"这一修复（旧实现里一个失灵的监听器就能让线程报废），
 * 以及 Error 一律回滚、提交失败必须显式报错这两条语义。
 *
 * @author TaoYu
 */
class TransactionalTemplateTest {

    private final TransactionalTemplate template = new TransactionalTemplate();

    @AfterEach
    void cleanThread() {
        TransactionContext.remove();
        TransactionContext.removeSynchronizations();
    }

    @SuppressWarnings("unchecked")
    private TransactionalInfo info(Class<?>[] rollbackFor, Class<?>[] noRollbackFor) {
        TransactionalInfo transactionalInfo = new TransactionalInfo();
        transactionalInfo.setPropagation(DsPropagation.REQUIRED);
        transactionalInfo.setRollbackFor((Class<? extends Throwable>[]) rollbackFor);
        transactionalInfo.setNoRollbackFor((Class<? extends Throwable>[]) noRollbackFor);
        return transactionalInfo;
    }

    private TransactionalExecutor executor(TransactionalInfo transactionalInfo, Throwing body) {
        return new TransactionalExecutor() {
            @Override
            public Object execute() throws Throwable {
                return body.run();
            }

            @Override
            public TransactionalInfo getTransactionInfo() {
                return transactionalInfo;
            }
        };
    }

    @FunctionalInterface
    private interface Throwing {
        Object run() throws Throwable;
    }

    /**
     * 记录回调相位的同步器，必须在事务激活后（execute 内部）注册。
     */
    private TransactionSynchronization recordingSync(List<String> phases) {
        return new TransactionSynchronization() {
            @Override
            public void beforeCompletion() {
                phases.add("beforeCompletion");
            }

            @Override
            public void beforeCommit(boolean readOnly) {
                phases.add("beforeCommit");
            }

            @Override
            public void afterCommit() {
                phases.add("afterCommit");
            }

            @Override
            public void afterCompletion(int status) {
                phases.add("afterCompletion:" + status);
            }
        };
    }

    @Test
    void errorFromBusinessMethodShouldRollBackNotCommit() {
        // 旧代码 catch (Exception)：Error 逃逸后 state 保持 true，坏事务被提交。
        List<String> phases = new ArrayList<>();
        TransactionalInfo transactionalInfo = info(new Class[]{Exception.class}, new Class[0]);
        assertThatThrownBy(() -> template.execute(executor(transactionalInfo, () -> {
            TransactionContext.registerSynchronization(recordingSync(phases));
            throw new NoClassDefFoundError("boom");
        }))).isInstanceOf(NoClassDefFoundError.class);

        // STATUS_ROLLED_BACK = 1
        assertThat(phases).containsExactly("beforeCompletion", "afterCompletion:1");
        assertThat(TransactionContext.getXID()).isNull();
        assertThat(TransactionContext.getSynchronizations()).isEmpty();
    }

    @Test
    void beforeCompletionListenerThrowingShouldNotStrandTheThread() throws Throwable {
        // 旧代码：finally 链上监听器抛错 → 后续 commit/rollback/清理全部跳过 → xid+连接+同步器
        // 永久残留在线程上，该线程之后的所有写静默丢失。
        List<String> phases = new ArrayList<>();
        TransactionSynchronization brokenSync = new TransactionSynchronization() {
            @Override
            public void beforeCompletion() {
                throw new IllegalStateException("listener broken");
            }

            @Override
            public void afterCompletion(int status) {
                phases.add("afterCompletion:" + status);
            }
        };
        Connection connection = mock(Connection.class);
        TransactionalInfo transactionalInfo = info(new Class[]{Exception.class}, new Class[0]);
        // 业务方法抛 RuntimeException（rollbackFor=Exception）→ state=false，随后 beforeCompletion 又抛错
        assertThatThrownBy(() -> template.execute(executor(transactionalInfo, () -> {
            TransactionContext.registerSynchronization(brokenSync);
            ConnectionFactory.putConnection(TransactionContext.getXID(), "ds", new ConnectionProxy(connection, "ds"));
            throw new RuntimeException("business failure");
        }))).isInstanceOf(RuntimeException.class).hasMessage("business failure");

        // 业务异常没有被监听器异常覆盖，且所有残留都被清理、连接被强制释放
        verify(connection).rollback();
        verify(connection).close();
        assertThat(TransactionContext.getXID()).isNull();
        assertThat(TransactionContext.getSynchronizations()).isEmpty();
        // STATUS_UNKNOWN = 2
        assertThat(phases).containsExactly("afterCompletion:2");
    }

    @Test
    void commitFailureShouldSurfaceAsTransactionSystemException() throws Throwable {
        // 业务方法成功但底层 commit 抛错：旧代码把异常从 finally 里裸抛（掩盖清理），
        // 新语义：先完成强制回滚/清理，再以 TransactionSystemException 告知调用方"成功"是假象。
        Connection connection = mock(Connection.class);
        doThrow(new SQLException("db gone")).when(connection).commit();
        TransactionalInfo transactionalInfo = info(new Class[0], new Class[0]);
        assertThatThrownBy(() -> template.execute(executor(transactionalInfo, () -> {
            ConnectionFactory.putConnection(TransactionContext.getXID(), "ds", new ConnectionProxy(connection, "ds"));
            return "ok";
        }))).isInstanceOf(TransactionSystemException.class).hasCauseInstanceOf(SQLException.class);

        // 连接在 notify() 的 finally 里已被归还（池化连接的 close 自带隐式回滚），不泄漏
        verify(connection).close();
        assertThat(TransactionContext.getXID()).isNull();
    }

    @Test
    void noRollbackForExceptionShouldCommitAndPropagateOriginalException() throws Throwable {
        List<String> phases = new ArrayList<>();
        TransactionalInfo transactionalInfo = info(new Class[0], new Class[]{IOException.class});
        Connection connection = mock(Connection.class);
        assertThatThrownBy(() -> template.execute(executor(transactionalInfo, () -> {
            TransactionContext.registerSynchronization(recordingSync(phases));
            ConnectionFactory.putConnection(TransactionContext.getXID(), "ds", new ConnectionProxy(connection, "ds"));
            throw new IOException("ignored by noRollbackFor");
        }))).isInstanceOf(IOException.class);

        // noRollbackFor 命中 → state=true → 正常提交，且原始业务异常照常抛出（不被吞、不被覆盖）
        verify(connection).commit();
        verify(connection).close();
        assertThat(phases).containsExactly("beforeCompletion", "beforeCommit", "afterCommit",
                "afterCompletion:" + TransactionSynchronization.STATUS_COMMITTED);
        assertThat(TransactionContext.getXID()).isNull();
    }
}
