package com.baomidou.dynamic.datasource.jta.transaction;


import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import javax.sql.DataSource;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;

/**
 * 构建-- mybatis --事务管理工厂
 */
public class ZhjwTransactionFactory extends SpringManagedTransactionFactory {

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel transactionIsolationLevel, boolean z) {
        return new ZhjwSpringManagedTransaction(dataSource);
    }


}
