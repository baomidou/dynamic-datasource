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
package com.baomidou.dynamic.datasource.common.service.tx;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.exception.TransactionException;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 传播契约测试的外层编排：每个方法先写一行外层 tx_probe，再按场景调用内层服务的不同传播行为，
 * 最后由测试断言两条数据的落库情况。内层调用全部跨 bean（经代理），保证切面真实生效。
 */
@Service
@DS("order")
public class PropagationOuterService {

    private final PropagationInnerService innerService;
    private final DataSource dataSource;

    public PropagationOuterService(PropagationInnerService innerService, DataSource dataSource) {
        this.innerService = innerService;
        this.dataSource = dataSource;
    }

    /**
     * 内层 REQUIRED 加入外层后抛异常，异常冒泡外层：整体回滚。
     */
    @DSTransactional
    public void outerWithRequiredInnerThrow(String outerTag, String innerTag) {
        insertProbe(outerTag);
        innerService.requiredInsertThenThrow(innerTag);
    }

    /**
     * 内层 REQUIRES_NEW 写后抛异常（独立回滚）被外层捕获，外层正常提交。
     */
    @DSTransactional
    public void outerCommitWithRequiresNewInnerRollback(String outerTag, String innerTag) {
        insertProbe(outerTag);
        try {
            innerService.requiresNewInsertThenThrow(innerTag);
        } catch (IllegalStateException expected) {
            // 内层 REQUIRES_NEW 已独立回滚，外层继续提交
        }
    }

    /**
     * 内层 REQUIRES_NEW 独立提交后外层抛异常：外层回滚不影响内层。
     */
    @DSTransactional
    public void outerRollbackWithRequiresNewCommitted(String outerTag, String innerTag) {
        insertProbe(outerTag);
        innerService.requiresNewInsert(innerTag);
        throw new IllegalStateException("outer-rollback");
    }

    /**
     * 内层 NESTED 写后抛异常（回滚到 savepoint）被外层捕获，外层正常提交。
     */
    @DSTransactional
    public void outerCommitWithNestedRollback(String outerTag, String innerTag) {
        insertProbe(outerTag);
        try {
            innerService.nestedInsertThenThrow(innerTag);
        } catch (IllegalStateException expected) {
            // 内层 NESTED 已回滚到 savepoint，外层继续提交
        }
    }

    /**
     * 内层 NESTED 成功（仅释放 savepoint）后外层抛异常：外层回滚连带内层。
     */
    @DSTransactional
    public void outerRollbackWithNestedSuccess(String outerTag, String innerTag) {
        insertProbe(outerTag);
        innerService.nestedInsert(innerTag);
        throw new IllegalStateException("outer-rollback");
    }

    /**
     * 内层 NOT_SUPPORTED 挂起外层并自动提交后，外层抛异常：外层回滚不影响内层。
     */
    @DSTransactional
    public void outerRollbackWithNotSupportedCommitted(String outerTag, String innerTag) {
        insertProbe(outerTag);
        innerService.notSupportedInsert(innerTag);
        throw new IllegalStateException("outer-rollback");
    }

    /**
     * 内层 SUPPORTS 加入外层事务后外层抛异常：一起回滚。
     */
    @DSTransactional
    public void outerRollbackWithSupportsJoin(String outerTag, String innerTag) {
        insertProbe(outerTag);
        innerService.supportsInsert(innerTag);
        throw new IllegalStateException("outer-rollback");
    }

    /**
     * 外层事务内调用 NEVER 内层：NEVER 违约抛异常（外层捕获），外层正常提交。
     */
    @DSTransactional
    public void outerCommitWithNeverViolation(String outerTag, String innerTag) {
        insertProbe(outerTag);
        try {
            innerService.neverInsert(innerTag);
        } catch (TransactionException expected) {
            // NEVER 在有事务时违约，内层未执行任何写入
        }
    }

    /**
     * 内层 REQUIRES_NEW 跨库（product）独立提交后外层抛异常：order 回滚不影响 product。
     */
    @DSTransactional
    public void outerRollbackWithRequiresNewAcrossDs(String outerTag, String innerTag) {
        insertProbe(outerTag);
        innerService.requiresNewInsertToProduct(innerTag);
        throw new IllegalStateException("outer-rollback");
    }

    private void insertProbe(String tag) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("insert into tx_probe(tag) values (?)")) {
            statement.setString(1, tag);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
