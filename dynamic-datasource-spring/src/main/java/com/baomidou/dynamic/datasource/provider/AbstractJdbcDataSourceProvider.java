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
package com.baomidou.dynamic.datasource.provider;

import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * JDBC数据源提供者(抽象)
 *
 * @author TaoYu
 * @since 2.1.2
 */
@Slf4j
public abstract class AbstractJdbcDataSourceProvider extends AbstractDataSourceProvider implements DynamicDataSourceProvider {

    /**
     * JDBC driver
     */
    private final String driverClassName;
    /**
     * JDBC url 地址
     */
    private final String url;
    /**
     * JDBC 用户名
     */
    private final String username;
    /**
     * JDBC 密码
     */
    private final String password;

    /**
     * 通过默认数据源创建器创建数据源
     *
     * @param defaultDataSourceCreator 默认数据源创建器
     * @param url                      数据源url
     * @param username                 用户名
     * @param password                 密码
     */
    public AbstractJdbcDataSourceProvider(DefaultDataSourceCreator defaultDataSourceCreator, String url, String username, String password) {
        this(defaultDataSourceCreator, null, url, username, password);
    }

    /**
     * 通过默认数据源创建器创建数据源
     *
     * @param defaultDataSourceCreator 默认数据源创建器
     * @param driverClassName          驱动类名
     * @param url                      数据源url
     * @param username                 用户名
     * @param password                 密码
     */
    public AbstractJdbcDataSourceProvider(DefaultDataSourceCreator defaultDataSourceCreator, String driverClassName, String url, String username, String password) {
        super(defaultDataSourceCreator);
        this.driverClassName = driverClassName;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    /**
     * 关闭资源
     *
     * @param con 资源
     */
    private static void closeResource(AutoCloseable con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                log.debug("Could not close ", ex);
            } catch (Throwable ex) {
                log.debug("Unexpected exception on closing", ex);
            }
        }
    }

    @Override
    public Map<String, DataSource> loadDataSources() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // 由于 SPI 的支持，现在已无需显示加载驱动了
            // 但在用户显示配置的情况下，进行主动加载
            if (!DsStrUtils.isEmpty(driverClassName)) {
                Class.forName(driverClassName);
                log.info("成功加载数据库驱动程序");
            }
            conn = DriverManager.getConnection(url, username, password);
            log.info("成功获取数据库连接");
            stmt = conn.createStatement();
            Map<String, DataSourceProperty> dataSourcePropertiesMap = executeStmt(stmt);
            return createDataSourceMap(dataSourcePropertiesMap);
        } catch (Exception e) {
            log.error("loadDataSources error", e);
        } finally {
            closeResource(conn);
            closeResource(stmt);
        }
        return null;
    }

    /**
     * 执行语句获得数据源参数
     *
     * @param statement 语句
     * @return 数据源参数
     * @throws SQLException sql异常
     */
    protected abstract Map<String, DataSourceProperty> executeStmt(Statement statement)
            throws SQLException;
}