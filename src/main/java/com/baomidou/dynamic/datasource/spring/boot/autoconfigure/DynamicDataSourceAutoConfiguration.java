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

import com.baomidou.dynamic.datasource.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.DynamicDataSourceStrategy;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.YmlDynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationAdvisor;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationInterceptor;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * DynamicDataSourceAutoConfiguration
 *
 * @author TaoYu Kanyuxia
 * @see DynamicDataSourceProvider
 * @see DynamicDataSourceStrategy
 * @see DynamicRoutingDataSource
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Import(DruidDynamicDataSourceConfiguration.class)
public class DynamicDataSourceAutoConfiguration {

    private final DynamicDataSourceProperties properties;

    public DynamicDataSourceAutoConfiguration(DynamicDataSourceProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceProvider dynamicDataSourceProvider() {
        return new YmlDynamicDataSourceProvider(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRoutingDataSource dynamicDataSource(DynamicDataSourceProvider dynamicDataSourceProvider) {
        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
        dynamicRoutingDataSource.setDynamicDataSourceProvider(dynamicDataSourceProvider);
        dynamicRoutingDataSource.setDynamicDataSourceStrategyClass(properties.getDynamicDataSourceStrategyClass());
        dynamicRoutingDataSource.setPrimary(properties.getPrimary());
        return dynamicRoutingDataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceAnnotationAdvisor dynamicDatasourceAnnotationAdvisor() {
        return new DynamicDataSourceAnnotationAdvisor(new DynamicDataSourceAnnotationInterceptor());
    }

}