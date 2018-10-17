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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.baomidou.dynamic.datasource.DataSourceCreator;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationAdvisor;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationInterceptor;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.YmlDynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidDynamicDataSourceConfiguration;
import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 动态数据源核心自动配置类
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

    @Autowired
    private DynamicDataSourceProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceProvider dynamicDataSourceProvider(DataSourceCreator dataSourceCreator) {
        return new YmlDynamicDataSourceProvider(properties, dataSourceCreator);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceCreator dataSourceCreater() {
        return new DataSourceCreator();
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRoutingDataSource dynamicRoutingDataSource(DynamicDataSourceProvider dynamicDataSourceProvider) {
        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
        dynamicRoutingDataSource.setPrimary(properties.getPrimary());
        dynamicRoutingDataSource.setStrategy(properties.getStrategy());
        dynamicRoutingDataSource.setProvider(dynamicDataSourceProvider);
        return dynamicRoutingDataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceAnnotationAdvisor dynamicDatasourceAnnotationAdvisor() {
        DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor(properties.isMpEnabled());
        DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor);
        advisor.setOrder(properties.getOrder());
        return advisor;
    }
}