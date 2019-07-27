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
package com.baomidou.dynamic.datasource;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.baomidou.dynamic.datasource.exception.ErrorCreateDataSourceException;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConfig;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidWallConfigUtil;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.hikari.HikariCpConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.util.StringUtils;

/**
 * 数据源创建器
 *
 * @author TaoYu
 * @since 2.3.0
 */
@Slf4j
public class DynamicDataSourceCreator {

  /**
   * DRUID数据源类
   */
  private static final String DRUID_DATASOURCE = "com.alibaba.druid.pool.DruidDataSource";
  /**
   * HikariCp数据源
   */
  private static final String HIKARI_DATASOURCE = "com.zaxxer.hikari.HikariDataSource";
  /**
   * JNDI数据源查找
   */
  private static final JndiDataSourceLookup JNDI_DATA_SOURCE_LOOKUP = new JndiDataSourceLookup();

  private static Method createMethod;
  private static Method typeMethod;
  private static Method urlMethod;
  private static Method usernameMethod;
  private static Method passwordMethod;
  private static Method driverClassNameMethod;
  private static Method buildMethod;

  /**
   * 是否存在druid
   */
  private static Boolean druidExists = false;
  /**
   * 是否存在hikari
   */
  private static Boolean hikariExists = false;

  static {
    //to support springboot 1.5 and 2.x
    Class<?> builderClass = null;
    try {
      builderClass = Class.forName("org.springframework.boot.jdbc.DataSourceBuilder");
    } catch (Exception ignored) {
    }
    if (builderClass == null) {
      try {
        builderClass = Class
            .forName("org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder");
      } catch (Exception e) {
      }
    }
    if (builderClass != null) {
      try {
        createMethod = builderClass.getDeclaredMethod("create");
        typeMethod = builderClass.getDeclaredMethod("type", Class.class);
        urlMethod = builderClass.getDeclaredMethod("url", String.class);
        usernameMethod = builderClass.getDeclaredMethod("username", String.class);
        passwordMethod = builderClass.getDeclaredMethod("password", String.class);
        driverClassNameMethod = builderClass.getDeclaredMethod("driverClassName", String.class);
        buildMethod = builderClass.getDeclaredMethod("build");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  static {
    try {
      Class.forName(DRUID_DATASOURCE);
      druidExists = true;
    } catch (ClassNotFoundException ignored) {
    }
    try {
      Class.forName(HIKARI_DATASOURCE);
      hikariExists = true;
    } catch (ClassNotFoundException ignored) {
    }
  }

  @Setter
  private DruidConfig druidGlobalConfig;
  @Setter
  private HikariCpConfig hikariGlobalConfig;

  @Autowired(required = false)
  private ApplicationContext applicationContext;
  @Setter
  private String globalPublicKey;

  /**
   * 创建数据源
   *
   * @param dataSourceProperty 数据源信息
   * @return 数据源
   */
  public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
    DataSource dataSource;
    //如果是jndi数据源
    String jndiName = dataSourceProperty.getJndiName();
    if (jndiName != null && !jndiName.isEmpty()) {
      dataSource = createJNDIDataSource(jndiName);
    } else {
      Class<? extends DataSource> type = dataSourceProperty.getType();
      if (type == null) {
        if (druidExists) {
          dataSource = createDruidDataSource(dataSourceProperty);
        } else if (hikariExists) {
          dataSource = createHikariDataSource(dataSourceProperty);
        } else {
          dataSource = createBasicDataSource(dataSourceProperty);
        }
      } else if (DRUID_DATASOURCE.equals(type.getName())) {
        dataSource = createDruidDataSource(dataSourceProperty);
      } else if (HIKARI_DATASOURCE.equals(type.getName())) {
        dataSource = createHikariDataSource(dataSourceProperty);
      } else {
        dataSource = createBasicDataSource(dataSourceProperty);
      }
    }
    String schema = dataSourceProperty.getSchema();
    if (StringUtils.hasText(schema)) {
      runScript(dataSource, schema, dataSourceProperty);
    }
    String data = dataSourceProperty.getData();
    if (StringUtils.hasText(data)) {
      runScript(dataSource, data, dataSourceProperty);
    }
    return dataSource;
  }

  /**
   * 创建基础数据源
   *
   * @param dataSourceProperty 数据源参数
   * @return 数据源
   */
  public DataSource createBasicDataSource(DataSourceProperty dataSourceProperty) {
    try {
      if (StringUtils.isEmpty(dataSourceProperty.getPublicKey())) {
        dataSourceProperty.setPublicKey(globalPublicKey);
      }
      Object o1 = createMethod.invoke(null);
      Object o2 = typeMethod.invoke(o1, dataSourceProperty.getType());
      Object o3 = urlMethod.invoke(o2, dataSourceProperty.getUrl());
      Object o4 = usernameMethod.invoke(o3, dataSourceProperty.getUsername());
      Object o5 = passwordMethod.invoke(o4, dataSourceProperty.getPassword());
      Object o6 = driverClassNameMethod.invoke(o5, dataSourceProperty.getDriverClassName());
      return (DataSource) buildMethod.invoke(o6);
    } catch (Exception e) {
      throw new ErrorCreateDataSourceException(
          "dynamic-datasource create basic database named " + dataSourceProperty.getPollName()
              + " error");
    }
  }

  /**
   * 创建JNDI数据源
   *
   * @param jndiName jndi数据源名称
   * @return 数据源
   */
  public DataSource createJNDIDataSource(String jndiName) {
    return JNDI_DATA_SOURCE_LOOKUP.getDataSource(jndiName);
  }

  /**
   * 创建DRUID数据源
   *
   * @param dataSourceProperty 数据源参数
   * @return 数据源
   */
  public DataSource createDruidDataSource(DataSourceProperty dataSourceProperty) {
    if (StringUtils.isEmpty(dataSourceProperty.getPublicKey())) {
      dataSourceProperty.setPublicKey(globalPublicKey);
    }
    DruidDataSource dataSource = new DruidDataSource();
    dataSource.setUsername(dataSourceProperty.getUsername());
    dataSource.setPassword(dataSourceProperty.getPassword());
    dataSource.setUrl(dataSourceProperty.getUrl());
    dataSource.setDriverClassName(dataSourceProperty.getDriverClassName());
    dataSource.setName(dataSourceProperty.getPollName());
    DruidConfig config = dataSourceProperty.getDruid();
    Properties properties = config.toProperties(druidGlobalConfig);
    String filters = properties.getProperty("druid.filters");
    List<Filter> proxyFilters = new ArrayList<>(2);
    if (!StringUtils.isEmpty(filters) && filters.contains("stat")) {
      StatFilter statFilter = new StatFilter();
      statFilter.configFromProperties(properties);
      proxyFilters.add(statFilter);
    }
    if (!StringUtils.isEmpty(filters) && filters.contains("wall")) {
      WallConfig wallConfig = DruidWallConfigUtil
          .toWallConfig(dataSourceProperty.getDruid().getWall(), druidGlobalConfig.getWall());
      WallFilter wallFilter = new WallFilter();
      wallFilter.setConfig(wallConfig);
      proxyFilters.add(wallFilter);
    }

    if (this.applicationContext != null) {
      for (String filterId : this.druidGlobalConfig.getProxyFilters()) {
        proxyFilters.add(this.applicationContext.getBean(filterId, Filter.class));
      }
    }
    dataSource.setProxyFilters(proxyFilters);
    dataSource.configFromPropety(properties);
    //连接参数单独设置
    dataSource.setConnectProperties(config.getConnectionProperties());
    //设置druid内置properties不支持的的参数
    Boolean testOnReturn = config.getTestOnReturn() == null ? druidGlobalConfig.getTestOnReturn()
        : config.getTestOnReturn();
    if (testOnReturn != null && testOnReturn.equals(true)) {
      dataSource.setTestOnReturn(true);
    }
    Integer validationQueryTimeout =
        config.getValidationQueryTimeout() == null ? druidGlobalConfig.getValidationQueryTimeout()
            : config.getValidationQueryTimeout();
    if (validationQueryTimeout != null && !validationQueryTimeout.equals(-1)) {
      dataSource.setValidationQueryTimeout(validationQueryTimeout);
    }

    Boolean sharePreparedStatements =
        config.getSharePreparedStatements() == null ? druidGlobalConfig.getSharePreparedStatements()
            : config.getSharePreparedStatements();
    if (sharePreparedStatements != null && sharePreparedStatements.equals(true)) {
      dataSource.setSharePreparedStatements(true);
    }
    Integer connectionErrorRetryAttempts =
        config.getConnectionErrorRetryAttempts() == null ? druidGlobalConfig
            .getConnectionErrorRetryAttempts() : config.getConnectionErrorRetryAttempts();
    if (connectionErrorRetryAttempts != null && !connectionErrorRetryAttempts.equals(1)) {
      dataSource.setConnectionErrorRetryAttempts(connectionErrorRetryAttempts);
    }
    Boolean breakAfterAcquireFailure =
        config.getBreakAfterAcquireFailure() == null ? druidGlobalConfig
            .getBreakAfterAcquireFailure() : config.getBreakAfterAcquireFailure();
    if (breakAfterAcquireFailure != null && breakAfterAcquireFailure.equals(true)) {
      dataSource.setBreakAfterAcquireFailure(true);
    }

    Integer timeout = config.getRemoveAbandonedTimeoutMillis() == null ?
        druidGlobalConfig.getRemoveAbandonedTimeoutMillis()
        : config.getRemoveAbandonedTimeoutMillis();
    if (timeout != null) {
      dataSource.setRemoveAbandonedTimeout(timeout);
    }

    Boolean abandoned = config.getRemoveAbandoned() == null ?
        druidGlobalConfig.getRemoveAbandoned() : config.getRemoveAbandoned();
    if (abandoned != null) {
      dataSource.setRemoveAbandoned(abandoned);
    }

    Boolean logAbandoned = config.getLogAbandoned() == null ?
        druidGlobalConfig.getLogAbandoned() : config.getLogAbandoned();
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

  /**
   * 创建Hikari数据源
   *
   * @param dataSourceProperty 数据源参数
   * @return 数据源
   * @author 离世庭院 小锅盖
   */
  public DataSource createHikariDataSource(DataSourceProperty dataSourceProperty) {
    if (StringUtils.isEmpty(dataSourceProperty.getPublicKey())) {
      dataSourceProperty.setPublicKey(globalPublicKey);
    }
    HikariCpConfig hikariCpConfig = dataSourceProperty.getHikari();
    HikariConfig config = hikariCpConfig.toHikariConfig(hikariGlobalConfig);
    config.setUsername(dataSourceProperty.getUsername());
    config.setPassword(dataSourceProperty.getPassword());
    config.setJdbcUrl(dataSourceProperty.getUrl());
    config.setDriverClassName(dataSourceProperty.getDriverClassName());
    config.setPoolName(dataSourceProperty.getPollName());
    return new HikariDataSource(config);
  }

  private void runScript(DataSource dataSource, String location,
      DataSourceProperty dataSourceProperty) {
    if (StringUtils.hasText(location)) {
      ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
      populator.setContinueOnError(dataSourceProperty.isContinueOnError());
      populator.setSeparator(dataSourceProperty.getSeparator());
      ClassPathResource resource = new ClassPathResource(location);
      if (resource.exists()) {
        populator.addScript(resource);
        try {
          DatabasePopulatorUtils.execute(populator, dataSource);
        } catch (Exception e) {
          log.warn("execute sql error", e);
        }
      } else {
        log.warn("could not find schema or data file {}", location);
      }
    }
  }
}
