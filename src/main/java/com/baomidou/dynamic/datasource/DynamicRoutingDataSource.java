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

import com.baomidou.dynamic.datasource.ds.AbstractRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.GroupDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import com.baomidou.dynamic.datasource.strategy.LoadBalanceDynamicDataSourceStrategy;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.p6spy.engine.spy.P6DataSource;
import io.seata.rm.datasource.DataSourceProxy;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心动态数据源组件
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource implements InitializingBean, DisposableBean {

    private static final String UNDERLINE = "_";
    /**
     * 所有数据库
     */
    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    /**
     * 分组数据库
     */
    private final Map<String, GroupDataSource> groupDataSources = new ConcurrentHashMap<>();
    @Setter
    private DynamicDataSourceProvider provider;
    @Setter
    private Class<? extends DynamicDataSourceStrategy> strategy = LoadBalanceDynamicDataSourceStrategy.class;
    @Setter
    private String primary = "master";
    @Setter
    private Boolean strict = false;
    @Setter
    private Boolean p6spy = false;
    @Setter
    private Boolean seata = false;

    @Override
    public DataSource determineDataSource() {
        return getDataSource(DynamicDataSourceContextHolder.peek());
    }

    private DataSource determinePrimaryDataSource() {
        log.debug("dynamic-datasource switch to the primary datasource");
        return groupDataSources.containsKey(primary) ? groupDataSources.get(primary).determineDataSource() : dataSourceMap.get(primary);
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
    public Map<String, GroupDataSource> getCurrentGroupDataSources() {
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
     * @param ds         数据源名称
     * @param dataSource 数据源
     */
    public synchronized void addDataSource(String ds, DataSource dataSource) {
        DataSource oldDataSource = dataSourceMap.put(ds, dataSource);
        // 新数据源添加到分组
        this.addGroupDataSource(ds, dataSource);
        // 关闭老的数据源
        if (oldDataSource != null) {
            try {
                closeDataSource(oldDataSource);
            } catch (Exception e) {
                log.error("dynamic-datasource - remove the database named [{}]  failed", ds, e);
            }
        }

        log.info("dynamic-datasource - load a datasource named [{}] success", ds);
    }

    /**
     * 新数据源添加到分组
     *
     * @param ds         新数据源的名字
     * @param dataSource 新数据源
     */
    private void addGroupDataSource(String ds, DataSource dataSource) {
        if (ds.contains(UNDERLINE)) {
            String group = ds.split(UNDERLINE)[0];
            GroupDataSource groupDataSource = groupDataSources.get(group);
            if (groupDataSource == null) {
                try {
                    groupDataSource = new GroupDataSource(group, strategy.getDeclaredConstructor().newInstance());
                    groupDataSources.put(group, groupDataSource);
                } catch (Exception e) {
                    throw new RuntimeException("dynamic-datasource - add the datasource named " + ds + " error", e);
                }
            }
            groupDataSource.addDatasource(ds, dataSource);
        }
    }

    /**
     * 删除数据源
     *
     * @param ds 数据源名称
     */
    public synchronized void removeDataSource(String ds) {
        if (!StringUtils.hasText(ds)) {
            throw new RuntimeException("remove parameter could not be empty");
        }
        if (primary.equals(ds)) {
            throw new RuntimeException("could not remove primary datasource");
        }
        if (dataSourceMap.containsKey(ds)) {
            DataSource dataSource = dataSourceMap.remove(ds);
            try {
                closeDataSource(dataSource);
            } catch (Exception e) {
                log.error("dynamic-datasource - remove the database named [{}]  failed", ds, e);
            }

            if (ds.contains(UNDERLINE)) {
                String group = ds.split(UNDERLINE)[0];
                if (groupDataSources.containsKey(group)) {
                    DataSource oldDataSource = groupDataSources.get(group).removeDatasource(ds);
                    if (oldDataSource == null) {
                        if (log.isWarnEnabled()) {
                            log.warn("fail for remove datasource from group. dataSource: {} ,group: {}", ds, group);
                        }
                    }
                }
            }
            log.info("dynamic-datasource - remove the database named [{}] success", ds);
        } else {
            log.warn("dynamic-datasource - could not find a database named [{}]", ds);
        }
    }

    /**
     * 关闭数据源。
     * <pre>
     *    从3.2.0开启，如果是原生或使用 DataSourceCreator 创建的数据源会包装成ItemDataSource。
     *    ItemDataSource保留了最原始的数据源，其可直接关闭。
     *    如果不是DataSourceCreator创建的数据源则只有尝试解包装再关闭。
     * </pre>
     */
    private void closeDataSource(DataSource dataSource) throws Exception {
        if (dataSource instanceof ItemDataSource) {
            ((ItemDataSource) dataSource).close();
        } else {
            if (seata && dataSource instanceof DataSourceProxy) {
                DataSourceProxy dataSourceProxy = (DataSourceProxy) dataSource;
                dataSource = dataSourceProxy.getTargetDataSource();
            }
            if (p6spy && dataSource instanceof P6DataSource) {
                Field realDataSourceField = P6DataSource.class.getDeclaredField("realDataSource");
                realDataSourceField.setAccessible(true);
                dataSource = (DataSource) realDataSourceField.get(dataSource);
            }
            Class<? extends DataSource> clazz = dataSource.getClass();
            Method closeMethod = clazz.getDeclaredMethod("close");
            closeMethod.invoke(dataSource);
        }
    }

    @Override
    public void destroy() throws Exception {
        log.info("dynamic-datasource start closing ....");
        for (Map.Entry<String, DataSource> item : dataSourceMap.entrySet()) {
            closeDataSource(item.getValue());
        }
        log.info("dynamic-datasource all closed success,bye");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 检查开启了配置但没有相关依赖
        checkEnv();
        // 添加并分组数据源
        Map<String, DataSource> dataSources = provider.loadDataSources();
        for (Map.Entry<String, DataSource> dsItem : dataSources.entrySet()) {
            addDataSource(dsItem.getKey(), dsItem.getValue());
        }
        // 检测默认数据源是否设置
        if (groupDataSources.containsKey(primary)) {
            log.info("dynamic-datasource initial loaded [{}] datasource,primary group datasource named [{}]", dataSources.size(), primary);
        } else if (dataSourceMap.containsKey(primary)) {
            log.info("dynamic-datasource initial loaded [{}] datasource,primary datasource named [{}]", dataSources.size(), primary);
        } else {
            throw new RuntimeException("dynamic-datasource Please check the setting of primary");
        }
    }

    private void checkEnv() {
        if (p6spy) {
            try {
                Class.forName("com.p6spy.engine.spy.P6DataSource");
                log.info("dynamic-datasource detect P6SPY plugin and enabled it");
            } catch (Exception e) {
                throw new RuntimeException("dynamic-datasource enabled P6SPY ,however without p6spy dependency", e);
            }
        }
        if (seata) {
            try {
                Class.forName("io.seata.rm.datasource.DataSourceProxy");
                log.info("dynamic-datasource detect ALIBABA SEATA and enabled it");
            } catch (Exception e) {
                throw new RuntimeException("dynamic-datasource enabled ALIBABA SEATA,however without seata dependency", e);
            }
        }
    }
}