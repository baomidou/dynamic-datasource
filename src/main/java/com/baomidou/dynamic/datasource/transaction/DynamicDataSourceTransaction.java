/**
 * Copyright © 2018 organization baomidou
 * <pre>
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
 * <pre/>
 */
package com.baomidou.dynamic.datasource.transaction;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.apache.ibatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 动态数据源事务
 *
 * @author Hccake
 * @since  3.3.0
 * @date 2020/9/4 14:38
 */
public class DynamicDataSourceTransaction implements Transaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transaction.class);

    private final DynamicRoutingDataSource dataSource;

    private Connection currentConnection;

    private final ConcurrentMap<String, Connection> connectionHolder = new ConcurrentHashMap<>();

    private boolean isConnectionTransactional;

    private boolean autoCommit;

    public DynamicDataSourceTransaction(DynamicRoutingDataSource dataSource) {
        Assert.notNull(dataSource, "No DataSource specified");
        this.dataSource = dataSource;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Connection getConnection() throws SQLException {
        openConnection();
        return this.currentConnection;
    }

    /**
     * Gets a connection from Spring transaction manager and discovers if this {@code Transaction} should manage
     * connection or let it to Spring.
     * <p>
     * It also reads autocommit setting because when using Spring Transaction MyBatis thinks that autocommit is always
     * false and will always call commit/rollback so we need to no-op that calls.
     */
    private void openConnection() throws SQLException {
        String dataSourceName = DynamicDataSourceContextHolder.peek();
        if (StringUtils.isEmpty(dataSourceName)) {
            dataSourceName = this.dataSource.getPrimary();
        }
        //连接为null 则为首次进入  需要获取一些必要信息
        if (this.currentConnection == null) {
            this.currentConnection = this.dataSource.getConnection();
            this.autoCommit = this.currentConnection.getAutoCommit();
            this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.currentConnection, this.dataSource);
            connectionHolder.put(dataSourceName, this.currentConnection);
        } else {
            if (connectionHolder.containsKey(dataSourceName)) {
                this.currentConnection = connectionHolder.get(dataSourceName);
            } else {
                this.currentConnection = this.dataSource.getConnection();
                connectionHolder.put(dataSourceName, this.currentConnection);
            }
        }

        LOGGER.debug("JDBC Connection [{}] will {} be managed by Spring", this.currentConnection, this.isConnectionTransactional ? " " : " not ");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws SQLException {
        if (this.currentConnection != null && !this.isConnectionTransactional && !this.autoCommit) {
            LOGGER.debug("Committing JDBC Connection [{}]", this.currentConnection);
            for (Connection conn : connectionHolder.values()) {
                conn.commit();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() throws SQLException {
        if (this.currentConnection != null && !this.isConnectionTransactional && !this.autoCommit) {
            LOGGER.debug("Rolling back JDBC Connection [{}]", this.currentConnection);
            for (Connection conn : connectionHolder.values()) {
                conn.rollback();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SQLException {
        for (Connection conn : connectionHolder.values()) {
            DataSourceUtils.releaseConnection(conn, this.dataSource);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getTimeout() throws SQLException {
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (holder != null && holder.hasTimeout()) {
            return holder.getTimeToLiveInSeconds();
        }
        return null;
    }


}
