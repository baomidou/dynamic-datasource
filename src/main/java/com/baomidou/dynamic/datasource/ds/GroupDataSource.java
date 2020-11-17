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
package com.baomidou.dynamic.datasource.ds;

import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import lombok.Data;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 组数据源
 *
 * @author TaoYu
 */
@Data
public class GroupDataSource {

    private String groupName;

    private DynamicDataSourceStrategy dynamicDataSourceStrategy;

    private Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    public GroupDataSource(String groupName, DynamicDataSourceStrategy dynamicDataSourceStrategy) {
        this.groupName = groupName;
        this.dynamicDataSourceStrategy = dynamicDataSourceStrategy;
    }

    /**
     * add a new datasource to this group
     *
     * @param ds         the name of the datasource
     * @param dataSource datasource
     */
    public DataSource addDatasource(String ds, DataSource dataSource) {
        return dataSourceMap.put(ds, dataSource);
    }

    /**
     * @param ds the name of the datasource
     */
    public DataSource removeDatasource(String ds) {
        return dataSourceMap.remove(ds);
    }

    public DataSource determineDataSource() {
        String dsKey = determineDsKey();
        return dataSourceMap.get(dsKey);
    }

    public String determineDsKey() {
        return dynamicDataSourceStrategy.determineDSKey(new ArrayList<>(dataSourceMap.keySet()));
    }

    public int size() {
        return dataSourceMap.size();
    }

}