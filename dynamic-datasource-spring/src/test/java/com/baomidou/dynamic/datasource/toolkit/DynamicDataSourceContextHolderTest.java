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

import static org.assertj.core.api.Assertions.assertThat;

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
}
