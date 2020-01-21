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
package com.baomidou.dynamic.datasource.creator;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.baomidou.dynamic.datasource.exception.ErrorCreateDataSourceException;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidSlf4jConfig;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidWallConfigUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

/**
 * @author TaoYu
 * @date 2020/1/21
 */
@Data
@AllArgsConstructor
public class DruidDataSourceCreator {

  private DruidConfig druidConfig;

  private ApplicationContext applicationContext;

  public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setUsername(dataSourceProperty.getUsername());
    dataSource.setPassword(dataSourceProperty.getPassword());
    dataSource.setUrl(dataSourceProperty.getUrl());
    dataSource.setDriverClassName(dataSourceProperty.getDriverClassName());
    dataSource.setName(dataSourceProperty.getPollName());
    DruidConfig config = dataSourceProperty.getDruid();
    Properties properties = config.toProperties(druidConfig);
    String filters = properties.getProperty("druid.filters");
    List<Filter> proxyFilters = new ArrayList<>(2);
    if (!StringUtils.isEmpty(filters) && filters.contains("stat")) {
      StatFilter statFilter = new StatFilter();
      statFilter.configFromProperties(properties);
      proxyFilters.add(statFilter);
    }
    if (!StringUtils.isEmpty(filters) && filters.contains("wall")) {
      WallConfig wallConfig = DruidWallConfigUtil.toWallConfig(dataSourceProperty.getDruid().getWall(), druidConfig.getWall());
      WallFilter wallFilter = new WallFilter();
      wallFilter.setConfig(wallConfig);
      proxyFilters.add(wallFilter);
    }
    if (!StringUtils.isEmpty(filters) && filters.contains("slf4j")) {
      Slf4jLogFilter slf4jLogFilter = new Slf4jLogFilter();
      // 由于properties上面被用了，LogFilter不能使用configFromProperties方法，这里只能一个个set了。
      DruidSlf4jConfig slf4jConfig = druidConfig.getSlf4j();
      slf4jLogFilter.setStatementLogEnabled(slf4jConfig.getEnable());
      slf4jLogFilter.setStatementExecutableSqlLogEnable(slf4jConfig.getStatementExecutableSqlLogEnable());
      proxyFilters.add(slf4jLogFilter);
    }

    if (this.applicationContext != null) {
      for (String filterId : druidConfig.getProxyFilters()) {
        proxyFilters.add(this.applicationContext.getBean(filterId, Filter.class));
      }
    }
    dataSource.setProxyFilters(proxyFilters);
    dataSource.configFromPropety(properties);
    //连接参数单独设置
    dataSource.setConnectProperties(config.getConnectionProperties());
    //设置druid内置properties不支持的的参数
    Boolean testOnReturn = config.getTestOnReturn() == null ? druidConfig.getTestOnReturn() : config.getTestOnReturn();
    if (testOnReturn != null && testOnReturn.equals(true)) {
      dataSource.setTestOnReturn(true);
    }
    Integer validationQueryTimeout =
        config.getValidationQueryTimeout() == null ? druidConfig.getValidationQueryTimeout() : config.getValidationQueryTimeout();
    if (validationQueryTimeout != null && !validationQueryTimeout.equals(-1)) {
      dataSource.setValidationQueryTimeout(validationQueryTimeout);
    }

    Boolean sharePreparedStatements =
        config.getSharePreparedStatements() == null ? druidConfig.getSharePreparedStatements() : config.getSharePreparedStatements();
    if (sharePreparedStatements != null && sharePreparedStatements.equals(true)) {
      dataSource.setSharePreparedStatements(true);
    }
    Integer connectionErrorRetryAttempts =
        config.getConnectionErrorRetryAttempts() == null ? druidConfig.getConnectionErrorRetryAttempts()
            : config.getConnectionErrorRetryAttempts();
    if (connectionErrorRetryAttempts != null && !connectionErrorRetryAttempts.equals(1)) {
      dataSource.setConnectionErrorRetryAttempts(connectionErrorRetryAttempts);
    }
    Boolean breakAfterAcquireFailure =
        config.getBreakAfterAcquireFailure() == null ? druidConfig.getBreakAfterAcquireFailure()
            : config.getBreakAfterAcquireFailure();
    if (breakAfterAcquireFailure != null && breakAfterAcquireFailure.equals(true)) {
      dataSource.setBreakAfterAcquireFailure(true);
    }

    Integer timeout = config.getRemoveAbandonedTimeoutMillis() == null ? druidConfig.getRemoveAbandonedTimeoutMillis()
        : config.getRemoveAbandonedTimeoutMillis();
    if (timeout != null) {
      dataSource.setRemoveAbandonedTimeout(timeout);
    }

    Boolean abandoned = config.getRemoveAbandoned() == null ? druidConfig.getRemoveAbandoned() : config.getRemoveAbandoned();
    if (abandoned != null) {
      dataSource.setRemoveAbandoned(abandoned);
    }

    Boolean logAbandoned = config.getLogAbandoned() == null ? druidConfig.getLogAbandoned() : config.getLogAbandoned();
    if (logAbandoned != null) {
      dataSource.setLogAbandoned(logAbandoned);
    }

    try {
      dataSource.init();
    } catch (SQLException e) {
      throw new ErrorCreateDataSourceException("druid create error", e);
    }
    return dataSource;
  }
}
