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
package com.baomidou.dynamic.datasource.tx;

import com.baomidou.dynamic.datasource.exception.TransactionException;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
public class TransactionalTemplate {

    public Object execute(TransactionalExecutor transactionalExecutor) throws Throwable {
        TransactionalInfo transactionInfo = transactionalExecutor.getTransactionInfo();
        DsPropagation propagation = transactionInfo.propagation;
        SuspendedResourcesHolder suspendedResourcesHolder = null;
        try {
            switch (propagation) {
                case NOT_SUPPORTED:
                    // If transaction is existing, suspend it.
                    if (existingTransaction()) {
                        suspendedResourcesHolder = suspend();
                    }
                    return transactionalExecutor.execute();
                case REQUIRES_NEW:
                    // If transaction is existing, suspend it, and then begin new transaction.
                    if (existingTransaction()) {
                        suspendedResourcesHolder = suspend();
                    }
                    // Continue and execute with new transaction
                    break;
                case SUPPORTS:
                    // If transaction is not existing, execute without transaction.
                    if (!existingTransaction()) {
                        return transactionalExecutor.execute();
                    }
                    // Continue and execute with new transaction
                    break;
                case REQUIRED:
                    // default
                    break;
                case NEVER:
                    // If transaction is existing, throw exception.
                    if (existingTransaction()) {
                        throw new TransactionException("Existing transaction found for transaction marked with propagation never");
                    } else {
                        // Execute without transaction and return.
                        return transactionalExecutor.execute();
                    }
                case MANDATORY:
                    // If transaction is not existing, throw exception.
                    if (!existingTransaction()) {
                        throw new TransactionException("No existing transaction found for transaction marked with propagation 'mandatory'");
                    }
                    // Continue and execute with current transaction.
                    break;
                case NESTED:
                    // If transaction is existing,Open a save point for child transaction rollback.
                    if (existingTransaction()) {
                        ConnectionFactory.createSavepoint(TransactionContext.getXID());
                    }
                    // Continue and execute with current transaction.
                    break;
                default:
                    throw new TransactionException("Not Supported Propagation:" + propagation);
            }
            return doExecute(transactionalExecutor);
        } finally {
            resume(suspendedResourcesHolder);
        }
    }

    private Object doExecute(TransactionalExecutor transactionalExecutor) throws Throwable {
        TransactionalInfo transactionInfo = transactionalExecutor.getTransactionInfo();
        DsPropagation propagation = transactionInfo.propagation;
        if (!StringUtils.isEmpty(TransactionContext.getXID())&&!propagation.equals(DsPropagation.NESTED)) {
            return transactionalExecutor.execute();
        }
        boolean state = true;
        Object o;
        String xid = LocalTxUtil.startTransaction();
        try {
            o = transactionalExecutor.execute();
        } catch (Exception e) {
            state = !isRollback(e, transactionInfo);
            throw e;
        } finally {
            if (state) {
                LocalTxUtil.commit(xid);
            } else {
                LocalTxUtil.rollback(xid);
            }
        }
        return o;
    }

    private boolean isRollback(Throwable e, TransactionalInfo transactionInfo) {
        boolean isRollback = true;
        Class<? extends Throwable>[] rollbacks = transactionInfo.rollbackFor;
        Class<? extends Throwable>[] noRollbackFor = transactionInfo.noRollbackFor;
        if (ArrayUtils.isNotEmpty(noRollbackFor)) {
            for (Class<? extends Throwable> noRollBack : noRollbackFor) {
                int depth = getDepth(e.getClass(), noRollBack);
                if (depth >= 0) {
                    return false;
                }
            }
        }
        if (ArrayUtils.isNotEmpty(rollbacks)) {
            for (Class<? extends Throwable> rollback : rollbacks) {
                int depth = getDepth(e.getClass(), rollback);
                if (depth >= 0) {
                    return isRollback;
                }
            }
        }
        return false;
    }

    private int getDepth(Class<?> exceptionClass, Class<? extends Throwable> rollback) {
        if (rollback == Throwable.class || rollback == Exception.class) {
            return 0;
        }
        // If we've gone as far as we can go and haven't found it...
        if (exceptionClass == Throwable.class) {
            return -1;
        }
        if (Objects.equals(exceptionClass, rollback)) {
            return 0;
        }
        return getDepth(exceptionClass.getSuperclass(), rollback);
    }

    private void resume(SuspendedResourcesHolder suspendedResourcesHolder) {
        if (suspendedResourcesHolder != null) {
            String xid = suspendedResourcesHolder.getXid();
            TransactionContext.bind(xid);
        }
    }

    public SuspendedResourcesHolder suspend() {
        String xid = TransactionContext.getXID();
        if (xid != null) {
            if (log.isInfoEnabled()) {
                log.info("Suspending current transaction, xid = {}", xid);
            }
            TransactionContext.unbind(xid);
            return new SuspendedResourcesHolder(xid);
        } else {
            return null;
        }
    }

    public boolean existingTransaction() {
        return !StringUtils.isEmpty(TransactionContext.getXID());
    }
}
