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

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.hikari.HikariCpConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Hikari数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/21
 */
@Data
@AllArgsConstructor
public class HikariDataSourceCreator {

  private HikariCpConfig hikariCpConfig;

  public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
    HikariConfig config = dataSourceProperty.getHikari().toHikariConfig(hikariCpConfig);
    config.setUsername(dataSourceProperty.getUsername());
    config.setPassword(dataSourceProperty.getPassword());
    config.setJdbcUrl(dataSourceProperty.getUrl());
    config.setDriverClassName(dataSourceProperty.getDriverClassName());
    config.setPoolName(dataSourceProperty.getPollName());
    return new HikariDataSource(config);
  }
}
