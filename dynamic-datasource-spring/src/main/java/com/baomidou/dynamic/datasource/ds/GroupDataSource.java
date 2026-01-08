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
package com.baomidou.dynamic.datasource.ds;

import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import lombok.Data;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 组数据源
 *
 * @author TaoYu
 */
@Data
public class GroupDataSource {

    /**
     * 组名
     */
    private String groupName;

    private DynamicDataSourceStrategy dynamicDataSourceStrategy;

    private Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();
    
    /**
     * Cached list of datasource keys to avoid recreating ArrayList on every call.
     * Marked as volatile to ensure visibility across threads.
     */
    private volatile List<String> cachedDsKeys = new ArrayList<>();

    public GroupDataSource(String groupName, DynamicDataSourceStrategy dynamicDataSourceStrategy) {
        this.groupName = groupName;
        this.dynamicDataSourceStrategy = dynamicDataSourceStrategy;
    }

    /**
     * add a new datasource to this group
     *
     * @param ds         the name of the datasource
     * @param dataSource datasource
     * @return the previous value associated with ds, or null if there was no mapping for ds.
     */
    public DataSource addDatasource(String ds, DataSource dataSource) {
        DataSource result = dataSourceMap.put(ds, dataSource);
        // Update cached keys list after modification
        cachedDsKeys = new ArrayList<>(dataSourceMap.keySet());
        return result;
    }

    /**
     * @param ds the name of the datasource
     */
    public DataSource removeDatasource(String ds) {
        DataSource result = dataSourceMap.remove(ds);
        // Update cached keys list after modification
        cachedDsKeys = new ArrayList<>(dataSourceMap.keySet());
        return result;
    }

    /**
     * determineDsKey
     * Performance optimized: uses cached list instead of creating new ArrayList on each call
     *
     * @return the name of the datasource
     */
    public String determineDsKey() {
        return dynamicDataSourceStrategy.determineKey(cachedDsKeys);
    }

    /**
     * determineDataSource
     *
     * @return the datasource
     */
    public DataSource determineDataSource() {
        return dataSourceMap.get(determineDsKey());
    }

    /**
     * size of this group
     *
     * @return the size of this group
     */
    public int size() {
        return dataSourceMap.size();
    }
}