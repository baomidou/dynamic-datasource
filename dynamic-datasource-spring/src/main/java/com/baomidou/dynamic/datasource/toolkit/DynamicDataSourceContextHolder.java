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

import org.springframework.core.NamedThreadLocal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/**
 * 核心基于ThreadLocal的切换数据源工具类
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
public final class DynamicDataSourceContextHolder {

    /**
     * 为什么要用链表存储(准确的是栈)
     * <pre>
     * 为了支持嵌套切换，如ABC三个service都是不同的数据源
     * 其中A的某个业务要调B的方法，B的方法需要调用C的方法。一级一级调用切换，形成了链。
     * 传统的只设置当前线程的方式不能满足此业务需求，必须使用栈，后进先出。
     * </pre>
     */
    private static final ThreadLocal<Deque<String>> LOOKUP_KEY_HOLDER = new NamedThreadLocal<Deque<String>>("dynamic-datasource") {
        @Override
        protected Deque<String> initialValue() {
            return new ArrayDeque<>();
        }
    };

    private DynamicDataSourceContextHolder() {
    }

    /**
     * 获得当前线程数据源
     *
     * @return 数据源名称
     */
    public static String peek() {
        return LOOKUP_KEY_HOLDER.get().peek();
    }

    /**
     * 设置当前线程数据源
     * <p>
     * 如非必要不要手动调用，调用后确保最终清除
     * </p>
     *
     * @param ds 数据源名称
     * @return 数据源名称
     */
    public static String push(String ds) {
        String dataSourceStr = DsStrUtils.isEmpty(ds) ? "" : ds;
        LOOKUP_KEY_HOLDER.get().push(dataSourceStr);
        return dataSourceStr;
    }

    /**
     * 清空当前线程数据源
     * <p>
     * 如果当前线程是连续切换数据源 只会移除掉当前线程的数据源名称
     * </p>
     */
    public static void poll() {
        Deque<String> deque = LOOKUP_KEY_HOLDER.get();
        deque.poll();
        if (deque.isEmpty()) {
            LOOKUP_KEY_HOLDER.remove();
        }
    }

    /**
     * 强制清空本地线程
     * <p>
     * 防止内存泄漏，如手动调用了push可调用此方法确保清除
     * </p>
     */
    public static void clear() {
        LOOKUP_KEY_HOLDER.remove();
    }

    /**
     * 包装任务，捕获当前线程的完整数据源栈，执行结束后恢复执行线程的原栈。
     * <p>只传播数据源名称，不传播事务或连接。每次执行使用独立副本，支持任务复用。</p>
     *
     * @param task 待执行任务
     * @return 在包装时捕获上下文的任务
     */
    public static Runnable decorate(Runnable task) {
        Objects.requireNonNull(task, "task");
        Deque<String> captured = new ArrayDeque<>(LOOKUP_KEY_HOLDER.get());
        return () -> {
            Deque<String> previous = LOOKUP_KEY_HOLDER.get();
            install(new ArrayDeque<>(captured));
            try {
                task.run();
            } finally {
                install(previous);
            }
        };
    }

    /**
     * 包装有返回值的任务，在包装时捕获完整数据源栈，执行结束后恢复原栈。
     * <p>只传播数据源名称，不传播事务或连接。</p>
     *
     * @param task 待执行任务
     * @param <T> 返回值类型
     * @return 包装后的任务
     */
    public static <T> Callable<T> wrap(Callable<T> task) {
        Objects.requireNonNull(task, "task");
        Deque<String> captured = new ArrayDeque<>(LOOKUP_KEY_HOLDER.get());
        return () -> {
            Deque<String> previous = LOOKUP_KEY_HOLDER.get();
            install(new ArrayDeque<>(captured));
            try {
                return task.call();
            } finally {
                install(previous);
            }
        };
    }

    /**
     * 包装执行器，每次提交任务时捕获提交线程的数据源栈。
     * <p>不改变执行器的生命周期、拒绝策略或事务边界。</p>
     *
     * @param executor 原执行器
     * @return 传播数据源上下文的执行器
     */
    public static Executor wrap(Executor executor) {
        Objects.requireNonNull(executor, "executor");
        return task -> executor.execute(decorate(task));
    }

    private static void install(Deque<String> stack) {
        if (stack.isEmpty()) {
            LOOKUP_KEY_HOLDER.remove();
        } else {
            LOOKUP_KEY_HOLDER.set(stack);
        }
    }
}
