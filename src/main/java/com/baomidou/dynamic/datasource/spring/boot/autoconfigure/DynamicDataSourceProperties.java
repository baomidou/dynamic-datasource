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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.baomidou.dynamic.datasource.DynamicDataSourceStrategy;
import com.baomidou.dynamic.datasource.LoadBalanceDynamicDataSourceStrategy;
import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DynamicDataSourceProperties
 *
 * @author TaoYu Kanyuxia
 * @see DataSourceProperties
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource.dynamic")
public class DynamicDataSourceProperties {

    /**
     * 必须设置默认的库,默认master
     */
    private String primary = "master";

    @NestedConfigurationProperty
    private Map<String, DynamicItemDataSourceProperties> datasource = new LinkedHashMap<>();

    /**
     * 多数据源选择算法clazz
     */
    private Class<? extends DynamicDataSourceStrategy> dynamicDataSourceStrategyClass = LoadBalanceDynamicDataSourceStrategy.class;

}
