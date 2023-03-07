package com.baomidou.dynamic.datasource.tx;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.transaction.Transaction;
import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;

import javax.sql.DataSource;

/**
 * Atomikos事务适配-多数据源切换
 *
 * @author <a href="mailto:312290710@qq.com">jiazhifeng</a>
 * @date 2023/03/02 10:20
 */
public class AtomikosTransactionFactory extends SpringManagedTransactionFactory {

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        DataSource determineDataSource = dataSource;

        // e.g:ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (dataSource instanceof DynamicRoutingDataSource) {
            determineDataSource = ((DynamicRoutingDataSource)dataSource).determineDataSource();
        }
        return new SpringManagedTransaction(determineDataSource);
    }
}
