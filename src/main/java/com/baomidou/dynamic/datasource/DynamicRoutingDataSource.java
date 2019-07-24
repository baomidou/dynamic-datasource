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

import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.p6spy.engine.spy.P6DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * 核心动态数据源组件
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource implements InitializingBean,
    DisposableBean {

  private static final String UNDERLINE = "_";

  @Setter
  private DynamicDataSourceProvider provider;
  @Setter
  private Class<? extends DynamicDataSourceStrategy> strategy;
  @Setter
  private String primary;
  @Setter
  private boolean strict;
  private boolean p6spy;

  /**
   * 所有数据库
   */
  private Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();
  /**
   * 分组数据库
   */
  private Map<String, DynamicGroupDataSource> groupDataSources = new ConcurrentHashMap<>();

  @Override
  public DataSource determineDataSource() {
    return getDataSource(DynamicDataSourceContextHolder.peek());
  }

  private DataSource determinePrimaryDataSource() {
    log.debug("dynamic-datasource switch to the primary datasource");
    return groupDataSources.containsKey(primary) ? groupDataSources.get(primary)
        .determineDataSource() : dataSourceMap.get(primary);
  }

  /**
   * 获取当前所有的数据源
   *
   * @return 当前所有数据源
   */
  public Map<String, DataSource> getCurrentDataSources() {
    return dataSourceMap;
  }

  /**
   * 获取的当前所有的分组数据源
   *
   * @return 当前所有的分组数据源
   */
  public Map<String, DynamicGroupDataSource> getCurrentGroupDataSources() {
    return groupDataSources;
  }

  /**
   * 获取数据源
   *
   * @param ds 数据源名称
   * @return 数据源
   */
  public DataSource getDataSource(String ds) {
    if (StringUtils.isEmpty(ds)) {
      return determinePrimaryDataSource();
    } else if (!groupDataSources.isEmpty() && groupDataSources.containsKey(ds)) {
      log.debug("dynamic-datasource switch to the datasource named [{}]", ds);
      return groupDataSources.get(ds).determineDataSource();
    } else if (dataSourceMap.containsKey(ds)) {
      log.debug("dynamic-datasource switch to the datasource named [{}]", ds);
      return dataSourceMap.get(ds);
    }
    if (strict) {
      throw new RuntimeException("dynamic-datasource could not find a datasource named" + ds);
    }
    return determinePrimaryDataSource();
  }

  /**
   * 添加数据源
   *
   * @param ds 数据源名称
   * @param dataSource 数据源
   */
  public synchronized void addDataSource(String ds, DataSource dataSource) {
    if (p6spy) {
      dataSource = new P6DataSource(dataSource);
    }
    dataSourceMap.put(ds, dataSource);
    if (ds.contains(UNDERLINE)) {
      String group = ds.split(UNDERLINE)[0];
      if (groupDataSources.containsKey(group)) {
        groupDataSources.get(group).addDatasource(dataSource);
      } else {
        try {
          DynamicGroupDataSource groupDatasource = new DynamicGroupDataSource(group,
              strategy.newInstance());
          groupDatasource.addDatasource(dataSource);
          groupDataSources.put(group, groupDatasource);
        } catch (Exception e) {
          log.error("dynamic-datasource - add the datasource named [{}] error", ds, e);
          dataSourceMap.remove(ds);
        }
      }
    }
    log.info("dynamic-datasource - load a datasource named [{}] success", ds);
  }

  /**
   * 删除数据源
   *
   * @param ds 数据源名称
   */
  public synchronized void removeDataSource(String ds) {
    if (dataSourceMap.containsKey(ds)) {
      DataSource dataSource = dataSourceMap.get(ds);
      dataSourceMap.remove(ds);
      if (ds.contains(UNDERLINE)) {
        String group = ds.split(UNDERLINE)[0];
        if (groupDataSources.containsKey(group)) {
          groupDataSources.get(group).removeDatasource(dataSource);
        }
      }
      log.info("dynamic-datasource - remove the database named [{}] success", ds);
    } else {
      log.warn("dynamic-datasource - could not find a database named [{}]", ds);
    }
  }

  public void setP6spy(boolean p6spy) {
    if (p6spy) {
      try {
        Class.forName("com.p6spy.engine.spy.P6DataSource");
        log.info("dynamic-datasource detect P6SPY plugin and enabled it");
        this.p6spy = true;
      } catch (Exception e) {
        log.warn("dynamic-datasource enabled P6SPY ,however without p6spy dependency");
      }
    } else {
      this.p6spy = false;
    }
  }

  @Override
  public void destroy() throws Exception {
    log.info("dynamic-datasource start closing ....");
    for (Map.Entry<String, DataSource> item : dataSourceMap.entrySet()) {
      DataSource dataSource = item.getValue();
      if (p6spy) {
        Field realDataSourceField = P6DataSource.class.getDeclaredField("realDataSource");
        realDataSourceField.setAccessible(true);
        dataSource = (DataSource) realDataSourceField.get(dataSource);
      }
      Class<? extends DataSource> clazz = dataSource.getClass();
      try {
        Method closeMethod = clazz.getDeclaredMethod("close");
        closeMethod.invoke(dataSource);
      } catch (NoSuchMethodException e) {
        log.warn("dynamic-datasource close the datasource named [{}] failed,", item.getKey());
      }
    }
    log.info("dynamic-datasource all closed success,bye");
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Map<String, DataSource> dataSources = provider.loadDataSources();
    //添加并分组数据源
    for (Map.Entry<String, DataSource> dsItem : dataSources.entrySet()) {
      addDataSource(dsItem.getKey(), dsItem.getValue());
    }
    //检测默认数据源设置
    if (groupDataSources.containsKey(primary)) {
      log.info(
          "dynamic-datasource initial loaded [{}] datasource,primary group datasource named [{}]",
          dataSources.size(), primary);
    } else if (dataSourceMap.containsKey(primary)) {
      log.info("dynamic-datasource initial loaded [{}] datasource,primary datasource named [{}]",
          dataSources.size(), primary);
    } else {
      throw new RuntimeException("dynamic-datasource Please check the setting of primary");
    }
  }
}