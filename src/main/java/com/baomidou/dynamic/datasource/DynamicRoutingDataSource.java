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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 核心动态数据源组件
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource implements InitializingBean {

    /**
     * 所有数据库
     */
    protected Map<String, DataSource> dataSourceMap = new LinkedHashMap<>();

    /**
     * 分组数据库
     */
    protected Map<String, DynamicGroupDataSource> groupDataSources = new HashMap<>();

    @Setter
    protected DynamicDataSourceProvider provider;

    @Setter
    protected Class<? extends DynamicDataSourceStrategy> strategy;

    @Setter
    protected String primary;

    @Override
    public DataSource determineDataSource() {
        String lookupKey = DynamicDataSourceContextHolder.getDataSourceLookupKey();
        if (StringUtils.isEmpty(lookupKey)) {
            return determinePrimaryDataSource();
        } else if (groupDataSources.containsKey(lookupKey) && !groupDataSources.isEmpty()) {
            log.debug("从 {} 组数据源中返回数据源", lookupKey);
            return groupDataSources.get(lookupKey).determineDataSource();
        } else if (dataSourceMap.containsKey(lookupKey)) {
            log.debug("从 {} 单数据源中返回数据源", lookupKey);
            return dataSourceMap.get(lookupKey);
        }
        return determinePrimaryDataSource();
    }

    private DataSource determinePrimaryDataSource() {
        log.debug("从默认数据源中返回数据");
        return groupDataSources.containsKey(primary) ? groupDataSources.get(primary).determineDataSource() : dataSourceMap.get(primary);
    }

    public synchronized void addDataSource(String dsName, DataSource dataSource) {
        dataSourceMap.put(dsName, dataSource);
        if (dsName.contains("_")) {
            String groupName = dsName.split("_")[0];
            if (groupDataSources.containsKey(groupName)) {
                groupDataSources.get(groupName).addDatasource(dataSource);
            } else {
                try {
                    DynamicGroupDataSource groupDatasource = new DynamicGroupDataSource(groupName, strategy.newInstance());
                    groupDatasource.addDatasource(dataSource);
                    groupDataSources.put(groupName, groupDatasource);
                } catch (Exception e) {
                    log.error("添加数据源失败", e);
                    dataSourceMap.remove(dsName);
                }
            }
        }
        log.info("添加数据源 {} 成功", dsName);
    }

    public synchronized void removeDataSource(String dsName, DataSource dataSource) {
        dataSourceMap.remove(dataSource);
        if (dsName.contains("_")) {
            String groupName = dsName.split("_")[0];
            if (groupDataSources.containsKey(groupName)) {
                groupDataSources.get(groupName).removeDatasource(dataSource);
            }
        }
        log.info("删除数据源 {} 成功", dsName);
    }

    @Override
    public void afterPropertiesSet() {
        Map<String, DataSource> dataSources = provider.loadDataSources();
        log.info("初始共加载 {} 个数据源", dataSources.size());
        //添加并分组数据源
        for (Map.Entry<String, DataSource> dsItem : dataSources.entrySet()) {
            addDataSource(dsItem.getKey(), dsItem.getValue());
        }
        //检测默认数据源设置
        if (groupDataSources.containsKey(primary)) {
            log.info("当前的默认数据源是组数据源,组名为 {} ，其下有 {} 个数据源", primary, groupDataSources.size());
        } else if (dataSourceMap.containsKey(primary)) {
            log.info("当前的默认数据源是单数据源，数据源名为{}", primary);
        } else {
            throw new RuntimeException("请检查primary默认数据库设置，当前未找到" + primary + "数据源");
        }
    }

}