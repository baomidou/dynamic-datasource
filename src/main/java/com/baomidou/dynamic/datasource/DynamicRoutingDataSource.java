/**
 * Copyright © 2018 organization 苞米豆
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

import com.baomidou.dynamic.datasource.util.DynamicDataSourceContextHolder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The core DynamicRoutingDataSource,It use determineCurrentLookupKey to determineDatasource.
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * 所有库
     */
    private Map<String, DataSource> dataSourceMap;

    /**
     * 分组数据库
     */
    private Map<String, GroupDatasource> groupDataSources = new HashMap<>();

    @Setter
    private DynamicDataSourceProvider dynamicDataSourceProvider;

    @Setter
    private Class<? extends DynamicDataSourceStrategy> dynamicDataSourceStrategyClass;

    /**
     * 默认数据源名称，默认master，可为组数据源名，可为单数据源名
     */
    @Setter
    private String primary;

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getDataSourceLookupKey();
    }

    @Override
    protected DataSource determineTargetDataSource() {
        String lookupKey = (String) determineCurrentLookupKey();
        if (groupDataSources.containsKey(lookupKey)) {
            log.debug("从 {} 组数据源中返回数据源", lookupKey);
            return groupDataSources.get(lookupKey).determineDataSource();
        } else if (dataSourceMap.containsKey(lookupKey)) {
            log.debug("从 {} 单数据源中返回数据源", lookupKey);
            return dataSourceMap.get(lookupKey);
        }
        log.debug("从默认数据源中返回数据");
        return groupDataSources.containsKey(primary) ? groupDataSources.get(lookupKey).determineDataSource() : dataSourceMap.get(primary);
    }

    @Override
    public void afterPropertiesSet() {
        this.dataSourceMap = dynamicDataSourceProvider.loadDataSources();
        log.debug("共加载 {} 个数据源", dataSourceMap.size());
        //分组数据源
        for (Map.Entry<String, DataSource> dsItem : dataSourceMap.entrySet()) {
            String dsName = dsItem.getKey();
            if (dsName.contains("_")) {
                String[] groupDs = dsName.split("_");
                String groupName = groupDs[0];
                if (groupDataSources.containsKey(groupName)) {
                    groupDataSources.get(groupName).addDatasource(dsItem.getValue());
                } else {
                    try {
                        GroupDatasource groupDatasource = new GroupDatasource(groupName, dynamicDataSourceStrategyClass.newInstance());
                        groupDatasource.addDatasource(dsItem.getValue());
                        groupDataSources.put(groupName, groupDatasource);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //检测组数据源设置
        Iterator<Map.Entry<String, GroupDatasource>> groupIterator = groupDataSources.entrySet().iterator();
        while (groupIterator.hasNext()) {
            Map.Entry<String, GroupDatasource> item = groupIterator.next();
            log.debug("组 {} 下有 {} 个数据源", item.getKey(), item.getValue().size());
            if (item.getValue().size() == 1) {
                log.warn("请注意不要设置一个只有一个数据源的组，{} 组将被移除", item.getKey());
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