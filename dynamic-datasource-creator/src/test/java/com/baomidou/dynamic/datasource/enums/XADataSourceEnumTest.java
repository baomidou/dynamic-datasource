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
package com.baomidou.dynamic.datasource.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link XADataSourceEnum} 支持的 XA 数据源枚举单元测试：类名映射与 {@code contains} 命中/未命中判断。
 *
 * @author TaoYu
 */
class XADataSourceEnumTest {

    @Test
    void shouldMapKnownXaDriverClassNames() {
        assertThat(XADataSourceEnum.MYSQL.getXaDriverClassName())
                .isEqualTo("com.mysql.cj.jdbc.MysqlXADataSource");
        assertThat(XADataSourceEnum.H2.getXaDriverClassName())
                .isEqualTo("org.h2.jdbcx.JdbcDataSource");
    }

    @Test
    void containsShouldMatchRegisteredClassName() {
        assertThat(XADataSourceEnum.contains("com.mysql.cj.jdbc.MysqlXADataSource")).isTrue();
        // 每个枚举项自身都应被识别
        for (XADataSourceEnum item : XADataSourceEnum.values()) {
            assertThat(XADataSourceEnum.contains(item.getXaDriverClassName())).isTrue();
        }
    }

    @Test
    void containsShouldRejectUnknownOrNull() {
        assertThat(XADataSourceEnum.contains("com.example.NotAnXADataSource")).isFalse();
        assertThat(XADataSourceEnum.contains(null)).isFalse();
        assertThat(XADataSourceEnum.contains("")).isFalse();
    }
}
