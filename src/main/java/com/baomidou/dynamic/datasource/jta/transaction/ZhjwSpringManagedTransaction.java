package com.baomidou.dynamic.datasource.jta.transaction;


import com.baomidou.dynamic.datasource.support.DdConstants;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.transaction.SpringManagedTransaction;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

/**
 * 接管spring的事务
 */
@Slf4j
public class ZhjwSpringManagedTransaction extends SpringManagedTransaction {

    private final DataSource dataSource;
    private Connection connection;
    private boolean isConnectionTransactional;
    private boolean autoCommit;
    private Map<String, Connection> CONNECTION_MAP = new ConcurrentHashMap();
    private String currentDbKey = getCurrentDbKey();

    public ZhjwSpringManagedTransaction(DataSource dataSource) {
        super(dataSource);
        this.dataSource = dataSource;
    }

    private String getCurrentDbKey() {
        String dsKey = DynamicDataSourceContextHolder.peek();
        dsKey = StringUtils.isEmpty(dsKey) ? getPrimary() : dsKey;
        return dsKey;
    }

    private String getPrimary() {
        return DdConstants.MASTER;
    }

    @Override
    public Connection getConnection() throws SQLException {
        log.info("\n\n执行到自定义事务管理器.............");
        String currentDbKey = getCurrentDbKey();
        if (Objects.equals(this.currentDbKey, currentDbKey)) {
            if (null == this.connection) {
                openCurrentConnection();
            }
            return this.connection;
        }
        Connection connection = this.CONNECTION_MAP.get(currentDbKey);
        if (null == connection || connection.isClosed()) {
            try {
                connection = this.dataSource.getConnection();
                this.CONNECTION_MAP.put(currentDbKey, connection);
            } catch (SQLException e) {
                throw new CannotGetJdbcConnectionException("不能获取JDBC数据源......", e);
            }
        }
        return connection;
    }

    private void openCurrentConnection() throws SQLException {
        this.connection = DataSourceUtils.getConnection(this.dataSource);
        this.autoCommit = this.connection.getAutoCommit();
        log.debug("当前JDBC链接自动提供 : {}", Boolean.valueOf(this.autoCommit));
        this.isConnectionTransactional = DataSourceUtils.isConnectionTransactional(this.connection, this.dataSource);
        log.debug(" 当前JDBC链接事务 = {}", Boolean.valueOf(this.isConnectionTransactional));
    }
    @Override
    public void commit() throws SQLException {
        log.info("执行到---------commit---------");
        super.commit();
    }

    @Override
    public void rollback() throws SQLException {
        if (!(this.connection == null || !this.isConnectionTransactional || this.autoCommit)) {
            log.debug("回滚JDBC链接 [{}]", this.connection);
            this.connection.rollback();
            for (Connection connection : this.CONNECTION_MAP.values()) {
                connection.rollback();
                log.debug("回滚JDBC链接 [{}]", this.connection);
            }
            log.debug("回滚JDBC链接 [{}]", this.connection);
        }
        log.info("执行到---------rollback---------",this.connection);
    }

    @Override
    public void close() throws SQLException {
        DataSourceUtils.releaseConnection(this.connection, this.dataSource);
        for (Connection connection : this.CONNECTION_MAP.values()) {
            DataSourceUtils.releaseConnection(connection, this.dataSource);
        }
    }

    @Override
    public Integer getTimeout() throws SQLException {
        ConnectionHolder holder = (ConnectionHolder) TransactionSynchronizationManager.getResource(dataSource);
        if (holder != null && holder.hasTimeout()) {
            return holder.getTimeToLiveInSeconds();
        }
        return null;
    }


}
