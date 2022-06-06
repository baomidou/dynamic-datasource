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

import com.baomidou.dynamic.datasource.tx.LocalTxUtil;
import com.baomidou.dynamic.datasource.tx.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.AopUtils;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.util.StringUtils;

/**
 * @author funkye
 */
@Slf4j
public class DynamicLocalTransactionInterceptor implements MethodInterceptor {

    private final TransactionAttributeSource transactionAttributeSource;

    public DynamicLocalTransactionInterceptor() {
        this(null);
    }

    public DynamicLocalTransactionInterceptor(TransactionAttributeSource transactionAttributeSource) {
        this.transactionAttributeSource = transactionAttributeSource;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        if (!StringUtils.isEmpty(TransactionContext.getXID())) {
            return methodInvocation.proceed();
        }
        boolean commitFlag = true;
        Object o;
        LocalTxUtil.startTransaction();
        try {
            o = methodInvocation.proceed();
        } catch (Exception e) {
            // 检查该异常是否可以回滚
            commitFlag = determineWhetherCommit(methodInvocation, e);
            throw e;
        } finally {
            if (commitFlag) {
                LocalTxUtil.commit();
            } else {
                LocalTxUtil.rollback();
            }
        }
        return o;
    }

    private boolean determineWhetherCommit(MethodInvocation invocation, Exception e) {
        if (transactionAttributeSource == null) {
            return false;
        }
        Class<?> targetClass = invocation.getThis() != null ? AopUtils.getTargetClass(invocation.getThis()) : null;
        TransactionAttribute transactionAttribute = transactionAttributeSource.getTransactionAttribute(invocation.getMethod(), targetClass);

        if (transactionAttribute == null) {
            return false;
        }

        boolean rollback = transactionAttribute.rollbackOn(e);
        return !rollback;
    }
}
