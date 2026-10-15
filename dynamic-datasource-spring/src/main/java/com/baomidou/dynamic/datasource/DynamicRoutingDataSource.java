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
package com.baomidou.dynamic.datasource;

import com.baomidou.dynamic.datasource.destroyer.DataSourceDestroyer;
import com.baomidou.dynamic.datasource.destroyer.DefaultDataSourceDestroyer;
import com.baomidou.dynamic.datasource.ds.AbstractRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.GroupDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.exception.CannotFindDataSourceException;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import com.baomidou.dynamic.datasource.strategy.LoadBalanceDynamicDataSourceStrategy;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.dynamic.datasource.unwrapper.DataSourceUnwrapperChain;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
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
    private final List<DynamicDataSourceProvider> providers;
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
    @Setter
    private Boolean graceDestroy = false;

    public DynamicRoutingDataSource(List<DynamicDataSourceProvider> providers) {
        this.providers = providers;
    }

    /**
     * 当前主数据源名称
     *
     * @return primary
     */
    @Override
    public String getPrimary() {
        return primary;
    }

    @Override
    public DataSource determineDataSource() {
        String dsKey = DynamicDataSourceContextHolder.peek();
        return getDataSource(dsKey);
    }


    private DataSource determinePrimaryDataSource() {
        log.debug("dynamic-datasource switch to the primary datasource");
        DataSource dataSource = dataSourceMap.get(primary);
        if (dataSource != null) {
            return dataSource;
        }
        GroupDataSource groupDataSource = groupDataSources.get(primary);
        if (groupDataSource != null) {
            return groupDataSource.determineDataSource();
        }
        throw new CannotFindDataSourceException("dynamic-datasource can not find primary datasource");
    }

    /**
     * 获取所有的数据源
     *
     * @return 当前所有数据源
     */
    public Map<String, DataSource> getDataSources() {
        return dataSourceMap;
    }

    /**
     * 获取的所有的分组数据源
     *
     * @return 当前所有的分组数据源
     */
    public Map<String, GroupDataSource> getGroupDataSources() {
        return groupDataSources;
    }

    /**
     * 获取数据源
     *
     * @param ds 数据源名称
     * @return 数据源
     */
    public DataSource getDataSource(String ds) {
        if (DsStrUtils.isEmpty(ds)) {
            return determinePrimaryDataSource();
        }
        // 每个 map 至多查一次：旧的 containsKey -> get 双读如果恰好被并发的 removeDataSource
        // 插入，会把 null 直接交给调用方（getConnection 时 NPE），strict 模式也拦不住。
        if (!groupDataSources.isEmpty()) {
            GroupDataSource groupDataSource = groupDataSources.get(ds);
            if (groupDataSource != null) {
                log.debug("dynamic-datasource switch to the datasource named [{}]", ds);
                return groupDataSource.determineDataSource();
            }
        }
        DataSource dataSource = dataSourceMap.get(ds);
        if (dataSource != null) {
            log.debug("dynamic-datasource switch to the datasource named [{}]", ds);
            return dataSource;
        }
        if (strict) {
            throw new CannotFindDataSourceException("dynamic-datasource could not find a datasource named " + ds);
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
            closeDataSource(ds, oldDataSource, graceDestroy);
        }
        log.info("dynamic-datasource - add a datasource named [{}] success", ds);
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
        if (!DsStrUtils.hasText(ds)) {
            throw new RuntimeException("remove parameter could not be empty");
        }
        if (primary.equals(ds)) {
            throw new RuntimeException("could not remove primary datasource");
        }
        if (dataSourceMap.containsKey(ds)) {
            DataSource dataSource = dataSourceMap.remove(ds);
            if (ds.contains(UNDERLINE)) {
                String group = ds.split(UNDERLINE)[0];
                if (groupDataSources.containsKey(group)) {
                    GroupDataSource groupDataSource = groupDataSources.get(group);
                    DataSource oldDataSource = groupDataSource.removeDatasource(ds);
                    if (oldDataSource == null) {
                        log.warn("fail for remove datasource from group. dataSource: {} ,group: {}", ds, group);
                    } else if (groupDataSource.size() == 0) {
                        // 空组会被后续路由命中：负载均衡/随机策略对空集合取模直接
                        // ArithmeticException(/ by zero)，删掉让语义回到正常的"找不到"分支。
                        groupDataSources.remove(group);
                    }
                }
            }
            closeDataSource(ds, dataSource, graceDestroy);
            log.info("dynamic-datasource - remove the database named [{}] success", ds);
        } else {
            log.warn("dynamic-datasource - could not find a database named [{}]", ds);
        }
    }

    @Override
    public void destroy() {
        log.info("dynamic-datasource start closing ....");
        for (Map.Entry<String, DataSource> item : dataSourceMap.entrySet()) {
            closeDataSource(item.getKey(), item.getValue(), false);
        }
        log.info("dynamic-datasource all closed success,bye");
    }

    @Override
    public void afterPropertiesSet() {
        // 检查开启了配置但没有相关依赖
        checkEnv();
        // 添加并分组数据源
        Map<String, DataSource> dataSources = new HashMap<>(16);
        for (DynamicDataSourceProvider provider : providers) {
            Map<String, DataSource> dsMap = provider.loadDataSources();
            if (dsMap != null) {
                dataSources.putAll(dsMap);
            }
        }
        for (Map.Entry<String, DataSource> dsItem : dataSources.entrySet()) {
            addDataSource(dsItem.getKey(), dsItem.getValue());
        }
        // 检测默认数据源是否设置
        if (groupDataSources.containsKey(primary)) {
            log.info("dynamic-datasource initial loaded [{}] datasource,primary group datasource named [{}]", dataSources.size(), primary);
        } else if (dataSourceMap.containsKey(primary)) {
            log.info("dynamic-datasource initial loaded [{}] datasource,primary datasource named [{}]", dataSources.size(), primary);
        } else {
            log.warn("dynamic-datasource initial loaded [{}] datasource,Please add your primary datasource or check your configuration", dataSources.size());
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

    /**
     * close db
     *
     * @param ds           dsName
     * @param dataSource   db
     * @param graceDestroy If true, close the connection after a delay.
     */
    private void closeDataSource(String ds, DataSource dataSource, boolean graceDestroy) {
        try {
            DataSource realDataSource = null;
            if (dataSource instanceof ItemDataSource) {
                realDataSource = ((ItemDataSource) dataSource).getRealDataSource();
            } else {
                realDataSource = new DataSourceUnwrapperChain(p6spy, seata).unwrap(dataSource);
            }

            if (null == realDataSource) {
                realDataSource = dataSource;
            }
            if (null != realDataSource) {
                DataSourceDestroyer destroyer = new DefaultDataSourceDestroyer();
                if (graceDestroy) {
                    destroyer.asyncDestroy(ds, realDataSource);
                } else {
                    destroyer.destroy(ds, realDataSource);
                }
            }
        } catch (Exception e) {
            log.warn("dynamic-datasource closed datasource named [{}] failed", ds, e);
        }
    }

}