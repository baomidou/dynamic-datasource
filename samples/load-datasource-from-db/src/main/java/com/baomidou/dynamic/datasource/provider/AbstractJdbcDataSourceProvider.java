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
package com.baomidou.dynamic.datasource.provider;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

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
  private String driverClassName;
  /**
   * JDBC url 地址
   */
  private String url;
  /**
   * JDBC 用户名
   */
  private String username;
  /**
   * JDBC 密码
   */
  private String password;

  public AbstractJdbcDataSourceProvider(String driverClassName, String url, String username, String password) {
    this.driverClassName = driverClassName;
    this.url = url;
    this.username = username;
    this.password = password;
  }

  @Override
  public Map<String, DataSource> loadDataSources() {
    Connection conn = null;
    Statement stmt = null;
    try {
      Class.forName(driverClassName);
      log.info("成功加载数据库驱动程序");
      conn = DriverManager.getConnection(url, username, password);
      log.info("成功获取数据库连接");
      stmt = conn.createStatement();
      Map<String, DataSourceProperty> dataSourcePropertiesMap = executeStmt(stmt);
      return createDataSourceMap(dataSourcePropertiesMap);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      JdbcUtils.closeConnection(conn);
      JdbcUtils.closeStatement(stmt);
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
