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
package com.baomidou.dynamic.datasource.toolkit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 数据源上下文栈语义单元测试：验证嵌套切换依赖的后进先出行为。
 *
 * @author TaoYu
 */
class DynamicDataSourceContextHolderTest {

    @AfterEach
    void tearDown() {
        DynamicDataSourceContextHolder.clear();
    }

    @Test
    void peekShouldBeNullWhenStackEmpty() {
        assertThat(DynamicDataSourceContextHolder.peek()).isNull();
    }

    @Test
    void pushShouldBeLastInFirstOut() {
        DynamicDataSourceContextHolder.push("a");
        DynamicDataSourceContextHolder.push("b");
        assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("b");

        // poll 只弹出栈顶，回退到上一层，支撑 A->B->A 的嵌套切换
        DynamicDataSourceContextHolder.poll();
        assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("a");

        DynamicDataSourceContextHolder.poll();
        assertThat(DynamicDataSourceContextHolder.peek()).isNull();
    }

    @Test
    void pushShouldCoerceNullAndEmptyToEmptyKey() {
        assertThat(DynamicDataSourceContextHolder.push(null)).isEmpty();
        assertThat(DynamicDataSourceContextHolder.peek()).isEmpty();
        DynamicDataSourceContextHolder.poll();

        assertThat(DynamicDataSourceContextHolder.push("")).isEmpty();
        assertThat(DynamicDataSourceContextHolder.peek()).isEmpty();
    }

    @Test
    void pushShouldReturnTheEffectiveKey() {
        assertThat(DynamicDataSourceContextHolder.push("master")).isEqualTo("master");
    }

    @Test
    void pollOnEmptyStackShouldNotThrow() {
        DynamicDataSourceContextHolder.poll();
        assertThat(DynamicDataSourceContextHolder.peek()).isNull();
    }

    @Test
    void clearShouldDropWholeStack() {
        DynamicDataSourceContextHolder.push("a");
        DynamicDataSourceContextHolder.push("b");
        DynamicDataSourceContextHolder.clear();
        assertThat(DynamicDataSourceContextHolder.peek()).isNull();
    }

    @Test
    void runnableShouldCaptureTheWholeStackAtDecorationTime() {
        DynamicDataSourceContextHolder.push("outer");
        DynamicDataSourceContextHolder.push("inner");
        Runnable task = DynamicDataSourceContextHolder.decorate(() -> {
            assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("inner");
            DynamicDataSourceContextHolder.poll();
            assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("outer");
            DynamicDataSourceContextHolder.clear();
        });
        DynamicDataSourceContextHolder.clear();
        DynamicDataSourceContextHolder.push("worker-outer");
        DynamicDataSourceContextHolder.push("worker-inner");
        task.run();
        // Reuse must get a fresh copy, even after the first execution cleared its copy.
        task.run();
        assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("worker-inner");
        DynamicDataSourceContextHolder.poll();
        assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("worker-outer");
    }

    @Test
    void emptySnapshotShouldHideAndRestoreTheWorkerStack() {
        Runnable task = DynamicDataSourceContextHolder.decorate(() -> {
            assertThat(DynamicDataSourceContextHolder.peek()).isNull();
            DynamicDataSourceContextHolder.push("temporary");
        });
        DynamicDataSourceContextHolder.push("worker");
        task.run();
        assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("worker");
    }

    @Test
    void runnableFailureShouldRestoreTheWorkerStack() {
        DynamicDataSourceContextHolder.push("captured");
        IllegalStateException failure = new IllegalStateException("task failure");
        Runnable task = DynamicDataSourceContextHolder.decorate(() -> {
            DynamicDataSourceContextHolder.clear();
            throw failure;
        });
        DynamicDataSourceContextHolder.clear();
        DynamicDataSourceContextHolder.push("worker");
        assertThatThrownBy(task::run).isSameAs(failure);
        assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("worker");
    }

    @Test
    void runnableErrorShouldRestoreAnEmptyWorkerStack() {
        DynamicDataSourceContextHolder.push("captured");
        AssertionError failure = new AssertionError("task error");
        Runnable task = DynamicDataSourceContextHolder.decorate(() -> {
            DynamicDataSourceContextHolder.push("temporary");
            throw failure;
        });
        DynamicDataSourceContextHolder.clear();
        assertThatThrownBy(task::run).isSameAs(failure);
        assertThat(DynamicDataSourceContextHolder.peek()).isNull();
    }

    @Test
    void callableShouldReturnItsValueAndRestoreTheWorkerStack() throws Exception {
        DynamicDataSourceContextHolder.push("captured");
        Callable<String> task = DynamicDataSourceContextHolder.wrap(DynamicDataSourceContextHolder::peek);
        DynamicDataSourceContextHolder.clear();
        assertThat(task.call()).isEqualTo("captured");
        assertThat(task.call()).isEqualTo("captured");
        assertThat(DynamicDataSourceContextHolder.peek()).isNull();
    }

    @Test
    void callableCheckedFailureShouldRestoreTheWorkerStack() {
        DynamicDataSourceContextHolder.push("captured");
        IOException failure = new IOException("task failure");
        Callable<String> task = DynamicDataSourceContextHolder.wrap(() -> {
            assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("captured");
            DynamicDataSourceContextHolder.clear();
            throw failure;
        });
        DynamicDataSourceContextHolder.clear();
        DynamicDataSourceContextHolder.push("worker");
        assertThatThrownBy(task::call).isSameAs(failure);
        assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("worker");
    }

    @Test
    void nestedInlineExecutionShouldRestoreEachEnclosingStack() {
        DynamicDataSourceContextHolder.push("caller");
        Runnable outer = DynamicDataSourceContextHolder.decorate(() -> {
            DynamicDataSourceContextHolder.push("nested");
            Runnable inner = DynamicDataSourceContextHolder.decorate(() -> {
                assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("nested");
                DynamicDataSourceContextHolder.clear();
            });
            inner.run();
            assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("nested");
            DynamicDataSourceContextHolder.poll();
        });
        outer.run();
        assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("caller");
        DynamicDataSourceContextHolder.poll();
        assertThat(DynamicDataSourceContextHolder.peek()).isNull();
    }

    @Test
    void executorShouldCaptureEachSubmissionRatherThanItsOwnCreation() {
        List<Runnable> queued = new ArrayList<>();
        Executor executor = DynamicDataSourceContextHolder.wrap(queued::add);
        List<String> observed = new ArrayList<>();
        DynamicDataSourceContextHolder.push("first");
        executor.execute(() -> observed.add(DynamicDataSourceContextHolder.peek()));
        DynamicDataSourceContextHolder.clear();
        DynamicDataSourceContextHolder.push("second");
        executor.execute(() -> observed.add(DynamicDataSourceContextHolder.peek()));
        DynamicDataSourceContextHolder.clear();
        queued.forEach(Runnable::run);
        assertThat(observed).containsExactly("first", "second");
        assertThat(DynamicDataSourceContextHolder.peek()).isNull();
    }

    @Test
    void rejectedSubmissionShouldLeaveTheCallerStackIntact() {
        RejectedExecutionException failure = new RejectedExecutionException("rejected");
        Executor executor = DynamicDataSourceContextHolder.wrap(task -> { throw failure; });
        DynamicDataSourceContextHolder.push("caller");
        assertThatThrownBy(() -> executor.execute(() -> {
            throw new AssertionError("Rejected task must not run");
        })).isSameAs(failure);
        assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("caller");
    }

    @Test
    void callerRunsRejectionPolicyShouldRestoreTheSubmittingThreadAfterFailure() throws Exception {
        CountDownLatch occupied = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new ThreadPoolExecutor.CallerRunsPolicy());
        try {
            Future<?> blocker = pool.submit(() -> {
                occupied.countDown();
                try {
                    assertThat(release.await(5, TimeUnit.SECONDS)).isTrue();
                } catch (InterruptedException failure) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(failure);
                }
            });
            assertThat(occupied.await(5, TimeUnit.SECONDS)).isTrue();
            Thread caller = Thread.currentThread();
            DynamicDataSourceContextHolder.push("caller");
            Executor executor = DynamicDataSourceContextHolder.wrap(pool);
            IllegalStateException failure = new IllegalStateException("inline failure");
            assertThatThrownBy(() -> executor.execute(() -> {
                assertThat(Thread.currentThread()).isSameAs(caller);
                assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("caller");
                DynamicDataSourceContextHolder.push("temporary");
                throw failure;
            })).isSameAs(failure);
            assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("caller");
            DynamicDataSourceContextHolder.poll();
            assertThat(DynamicDataSourceContextHolder.peek()).isNull();
            release.countDown();
            blocker.get(5, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            pool.shutdownNow();
            assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void concurrentExecutionsOfOneTaskShouldNotShareTheirMutableStack() throws Exception {
        DynamicDataSourceContextHolder.push("outer");
        DynamicDataSourceContextHolder.push("inner");
        CountDownLatch bothEntered = new CountDownLatch(2);
        Runnable task = DynamicDataSourceContextHolder.decorate(() -> {
            assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("inner");
            DynamicDataSourceContextHolder.poll();
            bothEntered.countDown();
            try {
                assertThat(bothEntered.await(5, TimeUnit.SECONDS)).isTrue();
            } catch (InterruptedException failure) {
                Thread.currentThread().interrupt();
                throw new AssertionError(failure);
            }
            assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("outer");
            DynamicDataSourceContextHolder.clear();
        });
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(task);
            Future<?> second = executor.submit(task);
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
            assertThat(DynamicDataSourceContextHolder.peek()).isEqualTo("inner");
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void nullTasksAndExecutorsShouldFailImmediately() {
        assertThatThrownBy(() -> DynamicDataSourceContextHolder.decorate(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DynamicDataSourceContextHolder.wrap((Callable<?>) null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> DynamicDataSourceContextHolder.wrap((Executor) null))
                .isInstanceOf(NullPointerException.class);
    }
}
