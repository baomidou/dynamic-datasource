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
package com.baomidou.dynamic.datasource.aop;

import com.baomidou.dynamic.datasource.support.DsTransactionSynchronization;
import com.baomidou.dynamic.datasource.support.DsTransactionSynchronizationManager;
import com.baomidou.dynamic.datasource.tx.LocalTxUtil;
import com.baomidou.dynamic.datasource.tx.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author funkye
 */
@Slf4j
public class DynamicLocalTransactionInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (!StringUtils.isEmpty(TransactionContext.getXID())) {
            return methodInvocation.proceed();
        }
        boolean state = true;
        Object o;
        try {
            DsTransactionSynchronizationManager.initSynchronization();
            LocalTxUtil.startTransaction();
            try {
                o = methodInvocation.proceed();
                //在事务提交之前添加业务逻辑
                List<DsTransactionSynchronization> synchronizations = DsTransactionSynchronizationManager.getSynchronizations();
                for (DsTransactionSynchronization synchronization : synchronizations) {
                    synchronization.beforeCompletion();
                }
            } catch (Exception e) {
                state = false;
                throw e;
            } finally {
                if (state) {
                    LocalTxUtil.commit();
                } else {
                    LocalTxUtil.rollback();
                }
            }
            //在事务提交完成后添加业务逻辑
            List<DsTransactionSynchronization> synchronizations = DsTransactionSynchronizationManager.getSynchronizations();
            for (DsTransactionSynchronization synchronization : synchronizations) {
                synchronization.afterCompletion(state);
            }
        }finally {
            DsTransactionSynchronizationManager.clear();
        }
        return o;
    }
}
