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
package com.baomidou.dynamic.datasource.support;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link ScriptRunner} 单元测试：钉死 continueOnError=false 时初始化失败必须向上抛
 * （旧实现把 DataAccessException 一律 warn 掉，坏脚本静默放行，首查才报"表不存在"），
 * 以及 continueOnError=true 时只告警不抛的对照语义。
 *
 * @author TaoYu
 */
class ScriptRunnerTest {

    private DataSource unreachableDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("db down"));
        return dataSource;
    }

    @Test
    void shouldPropagateInitFailureWhenContinueOnErrorIsFalse() throws SQLException {
        ScriptRunner runner = new ScriptRunner(false, ";");
        assertThatThrownBy(() -> runner.runScript(unreachableDataSource(), "classpath*:db/no-such-*.sql"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void shouldOnlyWarnWhenContinueOnErrorIsTrue() throws SQLException {
        ScriptRunner runner = new ScriptRunner(true, ";");
        assertThatCode(() -> runner.runScript(unreachableDataSource(), "classpath*:db/no-such-*.sql"))
                .doesNotThrowAnyException();
    }
}
