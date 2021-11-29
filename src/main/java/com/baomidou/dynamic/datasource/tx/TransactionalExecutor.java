package com.baomidou.dynamic.datasource.tx;



public interface TransactionalExecutor {

    Object execute() throws Throwable;

    TransactionalInfo getTransactionInfo();
}
