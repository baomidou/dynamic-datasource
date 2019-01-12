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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 核心动态数据源组件
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {


    private static final String UNDERLINE = "_";
    @Setter
    protected DynamicDataSourceProvider provider;
    @Setter
    protected Class<? extends DynamicDataSourceStrategy> strategy;
    @Setter
    protected String primary;
    protected boolean p6spy;
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
        log.debug("从默认数据源中返回数据");
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
            log.debug("从 {} 组数据源中返回数据源", ds);
            return groupDataSources.get(ds).determineDataSource();
        } else if (dataSourceMap.containsKey(ds)) {
            log.debug("从 {} 单数据源中返回数据源", ds);
            return dataSourceMap.get(ds);
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
                    DynamicGroupDataSource groupDatasource = new DynamicGroupDataSource(group, strategy.newInstance());
                    groupDatasource.addDatasource(dataSource);
                    groupDataSources.put(group, groupDatasource);
                } catch (Exception e) {
                    log.error("添加数据源失败", e);
                    dataSourceMap.remove(ds);
                }
            }
        }
        log.info("动态数据源-加载 {} 成功", ds);
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
            log.info("动态数据源-删除 {} 成功", ds);
        } else {
            log.warn("动态数据源-未找到 {} 数据源");
        }
    }

    public void setP6spy(boolean p6spy) {
        if (p6spy) {
            try {
                Class.forName("com.p6spy.engine.spy.P6DataSource");
                log.info("动态数据源-检测到并开启了p6spy");
                this.p6spy = true;
            } catch (Exception e) {
                log.warn("多数据源启动器开启了p6spy但并未引入相关依赖");
            }
        } else {
            this.p6spy = false;
        }
    }

    public void init() {
        Map<String, DataSource> dataSources = provider.loadDataSources();
        log.info("初始共加载 {} 个数据源", dataSources.size());
        //添加并分组数据源
        for (Map.Entry<String, DataSource> dsItem : dataSources.entrySet()) {
            addDataSource(dsItem.getKey(), dsItem.getValue());
        }
        //检测默认数据源设置
        if (groupDataSources.containsKey(primary)) {
            log.info("当前的默认数据源是组数据源,组名为 {} ，其下有 {} 个数据源", primary, groupDataSources.get(primary).size());
        } else if (dataSourceMap.containsKey(primary)) {
            log.info("当前的默认数据源是单数据源，数据源名为 {}", primary);
        } else {
            throw new RuntimeException("请检查primary默认数据库设置");
        }
    }
}