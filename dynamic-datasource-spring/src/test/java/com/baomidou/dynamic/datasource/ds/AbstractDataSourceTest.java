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
package com.baomidou.dynamic.datasource.ds;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * {@link AbstractDataSource} 基类默认行为单元测试：登录超时固定 0、LogWriter/超时设置不支持、
 * JDBC4 Wrapper 语义、父日志器返回。用一个最小具体子类承载抽象方法。
 *
 * @author TaoYu
 */
class AbstractDataSourceTest {

    /** 最小可用子类，仅补齐两个 getConnection 抽象方法。 */
    static class ProbeDataSource extends AbstractDataSource {
        final Connection connection = mock(Connection.class);

        @Override
        public Connection getConnection() {
            return connection;
        }

        @Override
        public Connection getConnection(String username, String password) {
            return connection;
        }
    }

    private final ProbeDataSource ds = new ProbeDataSource();

    @Test
    void loginTimeoutShouldBeZero() throws SQLException {
        assertThat(ds.getLoginTimeout()).isZero();
    }

    @Test
    void setLoginTimeoutShouldBeUnsupported() {
        assertThatThrownBy(() -> ds.setLoginTimeout(5))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void logWriterAccessorsShouldBeUnsupported() {
        assertThatThrownBy(ds::getLogWriter).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ds.setLogWriter(new PrintWriter(new StringWriter())))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void unwrapShouldReturnSelfForMatchingInterface() throws SQLException {
        assertThat(ds.unwrap(ProbeDataSource.class)).isSameAs(ds);
        assertThat(ds.unwrap(DataSource.class)).isSameAs(ds);
    }

    @Test
    void unwrapShouldThrowForIncompatibleInterface() {
        assertThatThrownBy(() -> ds.unwrap(Runnable.class))
                .isInstanceOf(SQLException.class);
    }

    @Test
    void isWrapperForShouldReflectInterfaceAssignment() throws SQLException {
        assertThat(ds.isWrapperFor(DataSource.class)).isTrue();
        assertThat(ds.isWrapperFor(CharSequence.class)).isFalse();
    }

    @Test
    void parentLoggerShouldNotBeNull() {
        assertThat(ds.getParentLogger()).isNotNull();
    }
}
