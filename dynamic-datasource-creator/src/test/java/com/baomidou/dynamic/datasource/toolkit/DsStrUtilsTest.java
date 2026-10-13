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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 字符串工具类单元测试。
 *
 * @author TaoYu
 */
class DsStrUtilsTest {

    @Test
    void isEmptyShouldOnlyTreatNullAndEmptyAsEmpty() {
        assertThat(DsStrUtils.isEmpty(null)).isTrue();
        assertThat(DsStrUtils.isEmpty("")).isTrue();
        // 与 hasText 不同，isEmpty 不忽略空白字符
        assertThat(DsStrUtils.isEmpty(" ")).isFalse();
        assertThat(DsStrUtils.isEmpty("a")).isFalse();
    }

    @Test
    void hasLengthShouldOnlyCheckNullAndEmpty() {
        assertThat(DsStrUtils.hasLength((String) null)).isFalse();
        assertThat(DsStrUtils.hasLength("")).isFalse();
        assertThat(DsStrUtils.hasLength(" ")).isTrue();
        assertThat(DsStrUtils.hasLength("a")).isTrue();
    }

    @Test
    void hasTextShouldTrimWhitespace() {
        assertThat(DsStrUtils.hasText(null)).isFalse();
        assertThat(DsStrUtils.hasText("")).isFalse();
        assertThat(DsStrUtils.hasText("   ")).isFalse();
        assertThat(DsStrUtils.hasText(" a ")).isTrue();
        assertThat(DsStrUtils.hasText("\t\n")).isFalse();
    }

    @Test
    void defaultIfBlankShouldFallBackOnlyForBlankInput() {
        assertThat(DsStrUtils.defaultIfBlank(null, "def")).isEqualTo("def");
        assertThat(DsStrUtils.defaultIfBlank("", "def")).isEqualTo("def");
        assertThat(DsStrUtils.defaultIfBlank("  ", "def")).isEqualTo("def");
        assertThat(DsStrUtils.defaultIfBlank("x", "def")).isEqualTo("x");
    }
}
