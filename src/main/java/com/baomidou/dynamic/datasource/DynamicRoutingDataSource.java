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
import java.util.Iterator;
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
    private Map<String, DataSource> dataSourceMap;

    /**
     * 分组数据库
     */
    private Map<String, DynamicGroupDataSource> groupDataSources = new HashMap<>();

    @Setter
    private DynamicDataSourceProvider provider;

    @Setter
    private Class<? extends DynamicDataSourceStrategy> strategy;

    @Setter
    private String primary;

    @Override
    public DataSource determineDataSource() {
        String lookupKey = DynamicDataSourceContextHolder.getDataSourceLookupKey();
        if (StringUtils.isEmpty(lookupKey)) {
            return determinePrimaryDataSource();
        } else if (groupDataSources.containsKey(lookupKey)) {
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

    @Override
    public void afterPropertiesSet() {
        this.dataSourceMap = provider.loadDataSources();
        log.debug("共加载 {} 个数据源", dataSourceMap.size());
        //分组数据源
        for (Map.Entry<String, DataSource> dsItem : dataSourceMap.entrySet()) {
            String dsName = dsItem.getKey();
            if (dsName.contains("_")) {
                String[] groupDs = dsName.split("_");
                String groupName = groupDs[0];
                DataSource dataSource = dsItem.getValue();
                if (groupDataSources.containsKey(groupName)) {
                    groupDataSources.get(groupName).addDatasource(dataSource);
                } else {
                    try {
                        DynamicGroupDataSource groupDatasource = new DynamicGroupDataSource(groupName, strategy.newInstance());
                        groupDatasource.addDatasource(dataSource);
                        groupDataSources.put(groupName, groupDatasource);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //检测组数据源设置
        Iterator<Map.Entry<String, DynamicGroupDataSource>> groupIterator = groupDataSources.entrySet().iterator();
        while (groupIterator.hasNext()) {
            Map.Entry<String, DynamicGroupDataSource> item = groupIterator.next();
            log.debug("组 {} 下有 {} 个数据源", item.getKey(), item.getValue().size());
            if (item.getValue().size() == 1) {
                log.warn("请注意不要设置一个只有一个数据源的组，{} 组将被移除，您将不能使用 {} 来切换数据源", item.getKey(), item.getKey());
                groupIterator.remove();
            }
        }
        //检测默认数据源设置
        if (groupDataSources.containsKey(primary)) {
            log.debug("当前的默认数据源是组数据源,组名为 {} ，其下有 {} 个数据源", primary, groupDataSources.size());
        } else if (dataSourceMap.containsKey(primary)) {
            log.debug("当前的默认数据源是单数据源，数据源名为{}", primary);
        } else {
            throw new RuntimeException("请检查primary默认数据库设置，当前未找到" + primary + "数据源");
        }
    }

}