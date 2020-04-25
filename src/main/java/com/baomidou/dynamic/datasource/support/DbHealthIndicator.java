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
package com.baomidou.dynamic.datasource.support;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 数据库健康状况指标
 *
 * @author hubin
 */
public class DbHealthIndicator extends AbstractHealthIndicator {

  /**
   * 维护数据源健康状况
   */
  private static Map<String, Boolean> DB_HEALTH = new ConcurrentHashMap<>();
  /**
   * 当前执行数据源
   */
  private DataSource dataSource;

  public DbHealthIndicator(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * 获取数据源连接健康状况
   *
   * @param dataSource 数据源名称
   * @return 健康状况
   */
  public static boolean getDbHealth(String dataSource) {
    return DB_HEALTH.get(dataSource);
  }

  /**
   * 设置连接池健康状况
   *
   * @param dataSource 数据源名称
   * @param health     健康状况 false 不健康 true 健康
   * @return 设置状态
   */
  public static Boolean setDbHealth(String dataSource, boolean health) {
    return DB_HEALTH.put(dataSource, health);
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {
    if (dataSource instanceof DynamicRoutingDataSource) {
      Map<String, DataSource> dataSourceMap = ((DynamicRoutingDataSource) dataSource).getCurrentDataSources();
      // 循环检查当前数据源是否可用
      for (Map.Entry<String, DataSource> dataSource : dataSourceMap.entrySet()) {
        Integer result = 0;
        try {
          result = query(dataSource.getValue());
        } finally {
          DB_HEALTH.put(dataSource.getKey(), 1 == result);
          builder.withDetail(dataSource.getKey(), result);
        }
      }
    }
  }


  private Integer query(DataSource dataSource) {
    //todo 这里应该可以配置或者可重写？
    List<Integer> results = new JdbcTemplate(dataSource).query("SELECT 1", new RowMapper<Integer>() {

      @Override
      public Integer mapRow(ResultSet resultSet, int i) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columns = metaData.getColumnCount();
        if (columns != 1) {
          throw new IncorrectResultSetColumnCountException(1, columns);
        }
        return (Integer) JdbcUtils.getResultSetValue(resultSet, 1, Integer.class);
      }
    });
    return DataAccessUtils.requiredSingleResult(results);
  }
}