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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.GroupDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 只读运维端点：暴露当前所有数据源（含运行期动态增删的）、分组构成与全局开关状态，
 * 访问路径 {@code /actuator/dynamic-datasource}（需按 actuator 惯例显式暴露）。
 * <p>
 * 刻意不返回 URL / 用户名等连接串信息——URL 可能携带凭据，状态盘点不需要它们。
 */
@Endpoint(id = "dynamic-datasource")
public class DynamicDataSourceEndpoint {

    private final ObjectProvider<DataSource> dataSources;
    private final DynamicDataSourceProperties properties;

    public DynamicDataSourceEndpoint(ObjectProvider<DataSource> dataSources, DynamicDataSourceProperties properties) {
        this.dataSources = dataSources;
        this.properties = properties;
    }

    @ReadOperation
    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("primary", properties.getPrimary());
        result.put("strict", properties.getStrict());
        result.put("p6spy", properties.getP6spy());
        result.put("seata", properties.getSeata());

        DataSource dataSource = dataSources.getIfAvailable();
        if (!(dataSource instanceof DynamicRoutingDataSource)) {
            result.put("dataSources", Collections.emptyMap());
            result.put("groups", Collections.emptyMap());
            return result;
        }
        DynamicRoutingDataSource routingDataSource = (DynamicRoutingDataSource) dataSource;

        Map<String, Object> dataSourceSummary = new LinkedHashMap<>();
        for (Map.Entry<String, DataSource> entry : routingDataSource.getDataSources().entrySet()) {
            dataSourceSummary.put(entry.getKey(), poolType(entry.getValue()));
        }
        result.put("dataSources", dataSourceSummary);

        Map<String, Object> groupSummary = new LinkedHashMap<>();
        for (Map.Entry<String, GroupDataSource> entry : routingDataSource.getGroupDataSources().entrySet()) {
            Map<String, Object> group = new LinkedHashMap<>();
            group.put("size", entry.getValue().size());
            group.put("dataSources", new ArrayList<>(entry.getValue().getDataSourceMap().keySet()));
            groupSummary.put(entry.getKey(), group);
        }
        result.put("groups", groupSummary);
        return result;
    }

    private String poolType(DataSource dataSource) {
        DataSource target = dataSource;
        if (dataSource instanceof ItemDataSource) {
            target = ((ItemDataSource) dataSource).getRealDataSource();
        }
        return target.getClass().getSimpleName();
    }
}
