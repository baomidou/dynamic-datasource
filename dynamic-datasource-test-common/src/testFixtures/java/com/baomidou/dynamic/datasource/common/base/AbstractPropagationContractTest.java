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
import com.baomidou.dynamic.datasource.common.service.tx.PropagationInnerService;
import com.baomidou.dynamic.datasource.common.service.tx.PropagationOuterService;
import com.baomidou.dynamic.datasource.common.service.tx.TxProbeQueryService;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.exception.TransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * {@code @DSTransactional} 七种传播行为的可执行契约：每种行为通过「先写 tx_probe、再按场景抛异常/
 * 捕获」编排，最终以两条 tag 的已提交行数断言落库结果。
 * <p>
 * 版本无关：Boot2/3/4 的 starter 各以薄子类继承本类，跑的是同一套断言。
 * 断言依据（见 tx 包实现）：REQUIRED 加入当前事务；REQUIRES_NEW / NOT_SUPPORTED 挂起外层；
 * NESTED 仅操作 savepoint（随外层提交或回滚）；SUPPORTS 无事务时自动提交；MANDATORY / NEVER 违约即抛。
 * <p>
 * 配置 master 是为了让上下文带着主库启动：Boot3/4 的 actuator 在启动时创建 db 健康检查，调用的 {@code isWrapperFor}
 * 要先解析出主库，解析不到就启动失败。
 *
 * @author TaoYu
 */
@SpringBootTest(classes = DsTransactionalApplication.class, webEnvironment = RANDOM_PORT, properties = {
        "spring.datasource.dynamic.datasource.master.url=jdbc:h2:mem:tx_master",
        "spring.datasource.dynamic.datasource.master.username=sa",
        "spring.datasource.dynamic.datasource.master.driver-class-name=org.h2.Driver"
})
public abstract class AbstractPropagationContractTest {

    @Autowired
    DataSource dataSource;
    @Autowired
    DefaultDataSourceCreator dataSourceCreator;
    @Autowired
    PropagationOuterService outerService;
    @Autowired
    PropagationInnerService innerService;
    @Autowired
    TxProbeQueryService queryService;

    @BeforeEach
    public void ensureDataSources() {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        ensureDataSource(ds, "order");
        ensureDataSource(ds, "product");
    }

    private void ensureDataSource(DynamicRoutingDataSource ds, String poolName) {
        if (ds.getDataSources().containsKey(poolName)) {
            return;
        }
        DataSourceProperty property = new DataSourceProperty();
        property.setPoolName(poolName);
        property.setDriverClassName("org.h2.Driver");
        property.setUrl("jdbc:h2:mem:" + poolName + ";INIT=RUNSCRIPT FROM 'classpath:db/ds-with-transactional.sql'");
        property.setUsername("sa");
        property.setPassword("");
        ds.addDataSource(poolName, dataSourceCreator.createDataSource(property));
    }

    @Test
    public void requiredInnerThrowRollsBackEverything() {
        String outer = "p_required_outer";
        String inner = "p_required_inner";
        // 内层 REQUIRED 加入外层并抛异常：整体回滚
        assertThatThrownBy(() -> outerService.outerWithRequiredInnerThrow(outer, inner))
                .isInstanceOf(IllegalStateException.class);
        assertThat(queryService.countOrder(outer)).isZero();
        assertThat(queryService.countOrder(inner)).isZero();
    }

    @Test
    public void requiresNewInnerRollbackKeepsOuterCommit() {
        String outer = "p_reqnew_outer_commit";
        String inner = "p_reqnew_inner_rollback";
        // 内层 REQUIRES_NEW 独立回滚，外层继续提交
        outerService.outerCommitWithRequiresNewInnerRollback(outer, inner);
        assertThat(queryService.countOrder(outer)).isOne();
        assertThat(queryService.countOrder(inner)).isZero();
    }

    @Test
    public void requiresNewSurvivesOuterRollback() {
        String outer = "p_reqnew_outer_rollback";
        String inner = "p_reqnew_inner_committed";
        // 内层 REQUIRES_NEW 已独立提交：外层回滚不能带走它
        assertThatThrownBy(() -> outerService.outerRollbackWithRequiresNewCommitted(outer, inner))
                .isInstanceOf(IllegalStateException.class);
        assertThat(queryService.countOrder(outer)).isZero();
        assertThat(queryService.countOrder(inner)).isOne();
    }

    @Test
    public void nestedInnerRollbackKeepsOuterCommit() {
        String outer = "p_nested_outer_commit";
        String inner = "p_nested_inner_rollback";
        // 内层 NESTED 回滚到 savepoint，外层正常提交
        outerService.outerCommitWithNestedRollback(outer, inner);
        assertThat(queryService.countOrder(outer)).isOne();
        assertThat(queryService.countOrder(inner)).isZero();
    }

    @Test
    public void nestedDependsOnOuterCommit() {
        String outer = "p_nested_outer_rollback";
        String inner = "p_nested_inner_success";
        // 内层 NESTED 只释放 savepoint、不真提交：外层回滚连带内层
        assertThatThrownBy(() -> outerService.outerRollbackWithNestedSuccess(outer, inner))
                .isInstanceOf(IllegalStateException.class);
        assertThat(queryService.countOrder(outer)).isZero();
        assertThat(queryService.countOrder(inner)).isZero();
    }

    @Test
    public void notSupportedCommitsIndependently() {
        String outer = "p_notsup_outer";
        String inner = "p_notsup_inner";
        // 内层 NOT_SUPPORTED 挂起外层并以自动提交执行：外层回滚不能带走它
        assertThatThrownBy(() -> outerService.outerRollbackWithNotSupportedCommitted(outer, inner))
                .isInstanceOf(IllegalStateException.class);
        assertThat(queryService.countOrder(outer)).isZero();
        assertThat(queryService.countOrder(inner)).isOne();
    }

    @Test
    public void supportsJoinsExistingTransaction() {
        String outer = "p_supports_outer";
        String inner = "p_supports_inner";
        // 内层 SUPPORTS 加入外层事务：外层回滚时一起回滚
        assertThatThrownBy(() -> outerService.outerRollbackWithSupportsJoin(outer, inner))
                .isInstanceOf(IllegalStateException.class);
        assertThat(queryService.countOrder(outer)).isZero();
        assertThat(queryService.countOrder(inner)).isZero();
    }

    @Test
    public void supportsWithoutTransactionRunsAutocommit() {
        String tag = "p_supports_no_tx";
        // 无外层事务：SUPPORTS 以非事务方式执行，连接自动提交
        innerService.supportsInsert(tag);
        assertThat(queryService.countOrder(tag)).isOne();
    }

    @Test
    public void mandatoryWithoutTransactionThrows() {
        String tag = "p_mandatory_no_tx";
        // 无外层事务：MANDATORY 违约抛异常且不执行写入
        assertThatThrownBy(() -> innerService.mandatoryInsert(tag))
                .isInstanceOf(TransactionException.class);
        assertThat(queryService.countOrder(tag)).isZero();
    }

    @Test
    public void neverWithoutTransactionRunsAutocommit() {
        String tag = "p_never_no_tx";
        // 无外层事务：NEVER 合法，以非事务方式执行
        innerService.neverInsert(tag);
        assertThat(queryService.countOrder(tag)).isOne();
    }

    @Test
    public void neverWithinTransactionThrowsAndOuterCommits() {
        String outer = "p_never_outer";
        String inner = "p_never_inner";
        // 外层事务内调用 NEVER：违约抛异常（外层捕获），写入未执行，外层正常提交
        outerService.outerCommitWithNeverViolation(outer, inner);
        assertThat(queryService.countOrder(outer)).isOne();
        assertThat(queryService.countOrder(inner)).isZero();
    }

    @Test
    public void requiresNewAcrossDataSourcesSurvivesOuterRollback() {
        String outer = "p_across_outer";
        String inner = "p_across_inner_product";
        // 内层 REQUIRES_NEW 跨库独立提交到 product：order 回滚不跨库传染
        assertThatThrownBy(() -> outerService.outerRollbackWithRequiresNewAcrossDs(outer, inner))
                .isInstanceOf(IllegalStateException.class);
        assertThat(queryService.countOrder(outer)).isZero();
        assertThat(queryService.countProduct(inner)).isOne();
    }
}
