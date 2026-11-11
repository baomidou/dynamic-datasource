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
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.dynamic.datasource.tx.LocalTxUtil;
import com.baomidou.dynamic.datasource.tx.TransactionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Shared H2 routing and transaction-boundary assertions for Boot 2/3/4. */
@SpringBootTest(classes = ContextPropagationApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.dynamic.primary=master",
                "spring.datasource.dynamic.strict=true",
                "spring.datasource.dynamic.datasource.master.type=com.zaxxer.hikari.HikariDataSource",
                "spring.datasource.dynamic.datasource.master.driver-class-name=org.h2.Driver",
                "spring.datasource.dynamic.datasource.master.url=jdbc:h2:mem:ctx_master;DB_CLOSE_DELAY=-1",
                "spring.datasource.dynamic.datasource.master.username=sa",
                "spring.datasource.dynamic.datasource.replica.type=com.zaxxer.hikari.HikariDataSource",
                "spring.datasource.dynamic.datasource.replica.driver-class-name=org.h2.Driver",
                "spring.datasource.dynamic.datasource.replica.url=jdbc:h2:mem:ctx_replica;DB_CLOSE_DELAY=-1",
                "spring.datasource.dynamic.datasource.replica.username=sa"
        })
public abstract class AbstractContextPropagationTest {
    @Autowired
    private DynamicRoutingDataSource dataSource;

    @Autowired
    @Qualifier("applicationTaskExecutor")
    private AsyncTaskExecutor taskExecutor;

    private JdbcTemplate jdbc;

    @BeforeEach
    void prepareDatabases() {
        DynamicDataSourceContextHolder.clear();
        jdbc = new JdbcTemplate(dataSource);
        for (String name : new String[] {"master", "replica"}) {
            JdbcTemplate physical = new JdbcTemplate(dataSource.getDataSource(name));
            physical.execute("CREATE TABLE IF NOT EXISTS context_probe (id INT PRIMARY KEY, amount INT)");
            physical.update("DELETE FROM context_probe");
            physical.update("INSERT INTO context_probe (id, amount) VALUES (1, 0)");
        }
    }

    @AfterEach
    void clearContext() {
        DynamicDataSourceContextHolder.clear();
    }

    @Test
    void callableShouldRouteToTheCapturedDatabaseAndCleanTheReusedWorker() throws Exception {
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try {
            assertThat(worker.submit(this::database).get(5, TimeUnit.SECONDS)).isEqualTo("CTX_MASTER");
            DynamicDataSourceContextHolder.push("replica");
            // The control reaches JDBC, demonstrating the existing async fallback to primary.
            assertThat(worker.submit(this::database).get(5, TimeUnit.SECONDS)).isEqualTo("CTX_MASTER");
            Callable<String> task = DynamicDataSourceContextHolder.wrap(this::database);
            DynamicDataSourceContextHolder.clear();
            assertThat(worker.submit(task).get(5, TimeUnit.SECONDS)).isEqualTo("CTX_REPLICA");
            assertThat(worker.submit(this::database).get(5, TimeUnit.SECONDS)).isEqualTo("CTX_MASTER");
        } finally {
            stop(worker);
        }
    }

    @Test
    void failedTaskShouldRestoreTheWorkersPreviousRoutingStack() throws Exception {
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try {
            worker.submit(() -> {
                DynamicDataSourceContextHolder.push("replica");
                DynamicDataSourceContextHolder.push("master");
            }).get(5, TimeUnit.SECONDS);
            DynamicDataSourceContextHolder.push("replica");
            IllegalStateException failure = new IllegalStateException("task failure");
            Callable<String> task = DynamicDataSourceContextHolder.wrap(() -> {
                assertThat(database()).isEqualTo("CTX_REPLICA");
                DynamicDataSourceContextHolder.clear();
                throw failure;
            });
            assertThatThrownBy(() -> worker.submit(task).get(5, TimeUnit.SECONDS))
                    .hasCause(failure);
            assertThat(worker.submit(this::database).get(5, TimeUnit.SECONDS)).isEqualTo("CTX_MASTER");
            assertThat(worker.submit(() -> {
                DynamicDataSourceContextHolder.poll();
                return database();
            }).get(5, TimeUnit.SECONDS)).isEqualTo("CTX_REPLICA");
        } finally {
            try {
                worker.submit(DynamicDataSourceContextHolder::clear).get(5, TimeUnit.SECONDS);
            } finally {
                stop(worker);
            }
        }
    }

    @Test
    void wrappedSpringExecutorShouldRouteCompletableFutures() throws Exception {
        DynamicDataSourceContextHolder.push("replica");
        CompletableFuture<String> result = CompletableFuture.supplyAsync(() -> {
            assertSpringWorkerThread();
            return database();
        }, DynamicDataSourceContextHolder.wrap(taskExecutor));
        assertThat(result.get(5, TimeUnit.SECONDS)).isEqualTo("CTX_REPLICA");
        DynamicDataSourceContextHolder.clear();
        assertThat(taskExecutor.submit(this::database).get(5, TimeUnit.SECONDS)).isEqualTo("CTX_MASTER");
    }

    @Test
    void springTransactionShouldStayOnItsOwningThreadAndBoundConnection() {
        TransactionTemplate transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
        transaction.execute(status -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            jdbc.update("UPDATE context_probe SET amount=1 WHERE id=1");
            DynamicDataSourceContextHolder.push("replica");
            try {
                assertThat(taskExecutor.submit(DynamicDataSourceContextHolder.wrap(() -> {
                    assertSpringWorkerThread();
                    assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
                    assertThat(TransactionContext.getXID()).isNull();
                    return database();
                })).get(5, TimeUnit.SECONDS)).isEqualTo("CTX_REPLICA");
                // A new routing key cannot replace the parent's already-bound master connection.
                assertThat(database()).isEqualTo("CTX_MASTER");
                assertThat(jdbc.queryForObject("SELECT amount FROM context_probe WHERE id=1", Integer.class))
                        .isEqualTo(1);
            } catch (Exception failure) {
                throw new AssertionError(failure);
            } finally {
                DynamicDataSourceContextHolder.clear();
                status.setRollbackOnly();
            }
            return null;
        });
        assertThat(jdbc.queryForObject("SELECT amount FROM context_probe WHERE id=1", Integer.class)).isZero();
    }

    @Test
    void localTransactionIdAndUncommittedWorkShouldNotPropagate() throws Exception {
        String xid = LocalTxUtil.startTransaction();
        try {
            jdbc.update("UPDATE context_probe SET amount=1 WHERE id=1");
            DynamicDataSourceContextHolder.push("replica");
            assertThat(taskExecutor.submit(DynamicDataSourceContextHolder.wrap(() -> {
                assertSpringWorkerThread();
                assertThat(TransactionContext.getXID()).isNull();
                assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
                return database();
            })).get(5, TimeUnit.SECONDS)).isEqualTo("CTX_REPLICA");
            assertThat(TransactionContext.getXID()).isEqualTo(xid);
        } finally {
            DynamicDataSourceContextHolder.clear();
            LocalTxUtil.rollback(xid);
        }
        assertThat(TransactionContext.getXID()).isNull();
        assertThat(jdbc.queryForObject("SELECT amount FROM context_probe WHERE id=1", Integer.class)).isZero();
    }

    /** JDK 21 variants override this inside the actual Spring executor task. */
    protected void assertSpringWorkerThread() {
    }

    private String database() {
        return jdbc.queryForObject("SELECT DATABASE()", String.class);
    }

    private static void stop(ExecutorService executor) throws InterruptedException {
        executor.shutdownNow();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    }
}

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableAsync
class ContextPropagationApplication {
}
