/**
 * Copyright © 2020 organization humingfeng
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
package cn.humingfeng.dynamic.datasource.creator;

import cn.humingfeng.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import cn.humingfeng.dynamic.datasource.support.DdConstants;
import cn.humingfeng.dynamic.datasource.support.ScriptRunner;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * 数据源创建器
 *
 * @author HuMingfeng
 * @since 2.3.0
 */
@Slf4j
@Setter
public class DataSourceCreator {

  /**
   * 是否存在druid
   */
  private static Boolean druidExists = false;
  /**
   * 是否存在hikari
   */
  private static Boolean hikariExists = false;

  static {
    try {
      Class.forName(DdConstants.DRUID_DATASOURCE);
      druidExists = true;
    } catch (ClassNotFoundException ignored) {
    }
    try {
      Class.forName(DdConstants.HIKARI_DATASOURCE);
      hikariExists = true;
    } catch (ClassNotFoundException ignored) {
    }
  }

  private BasicDataSourceCreator basicDataSourceCreator;
  private GbaseDataSourceCreator gbaseDataSourceCreator;
  private JndiDataSourceCreator jndiDataSourceCreator;
  private HikariDataSourceCreator hikariDataSourceCreator;
  private DruidDataSourceCreator druidDataSourceCreator;
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
      } else if (DdConstants.DRUID_DATASOURCE.equals(type.getName())) {
        dataSource = createDruidDataSource(dataSourceProperty);
      } else if (DdConstants.HIKARI_DATASOURCE.equals(type.getName())) {
        dataSource = createHikariDataSource(dataSourceProperty);
      } else if (DdConstants.GBASE_DATASOURCE.equals(type.getName())) {
          dataSource = createGbaseDataSource(dataSourceProperty);
      } else {
        dataSource = createBasicDataSource(dataSourceProperty);
      }
    }
    this.runScrip(dataSourceProperty, dataSource);
    return dataSource;
  }

  private void runScrip(DataSourceProperty dataSourceProperty, DataSource dataSource) {
    String schema = dataSourceProperty.getSchema();
    String data = dataSourceProperty.getData();
    if (StringUtils.hasText(schema) || StringUtils.hasText(data)) {
      ScriptRunner scriptRunner = new ScriptRunner(dataSourceProperty.isContinueOnError(), dataSourceProperty.getSeparator());
      if (StringUtils.hasText(schema)) {
        scriptRunner.runScript(dataSource, schema);
      }
      if (StringUtils.hasText(data)) {
        scriptRunner.runScript(dataSource, data);
      }
    }
  }

  /**
   * 创建基础数据源
   *
   * @param dataSourceProperty 数据源参数
   * @return 数据源
   */
  public DataSource createBasicDataSource(DataSourceProperty dataSourceProperty) {
    if (StringUtils.isEmpty(dataSourceProperty.getPublicKey())) {
      dataSourceProperty.setPublicKey(globalPublicKey);
    }
    return basicDataSourceCreator.createDataSource(dataSourceProperty);
  }

    /**
     * 国产Gbase数据源
     *
     * @param dataSourceProperty 数据源参数
     * @return 数据源
     */
    public DataSource createGbaseDataSource(DataSourceProperty dataSourceProperty) {
        if (StringUtils.isEmpty(dataSourceProperty.getPublicKey())) {
            dataSourceProperty.setPublicKey(globalPublicKey);
        }
        return gbaseDataSourceCreator.createDataSource(dataSourceProperty);
    }

  /**
   * 创建JNDI数据源
   *
   * @param jndiName jndi数据源名称
   * @return 数据源
   */
  public DataSource createJNDIDataSource(String jndiName) {
    return jndiDataSourceCreator.createDataSource(jndiName);
  }

  /**
   * 创建Druid数据源
   *
   * @param dataSourceProperty 数据源参数
   * @return 数据源
   */
  public DataSource createDruidDataSource(DataSourceProperty dataSourceProperty) {
    if (StringUtils.isEmpty(dataSourceProperty.getPublicKey())) {
      dataSourceProperty.setPublicKey(globalPublicKey);
    }
    return druidDataSourceCreator.createDataSource(dataSourceProperty);
  }

  /**
   * 创建Hikari数据源
   *
   * @param dataSourceProperty 数据源参数
   * @return 数据源
   * @author HuMingfeng
   */
  public DataSource createHikariDataSource(DataSourceProperty dataSourceProperty) {
    if (StringUtils.isEmpty(dataSourceProperty.getPublicKey())) {
      dataSourceProperty.setPublicKey(globalPublicKey);
    }
    return hikariDataSourceCreator.createDataSource(dataSourceProperty);
  }
}
