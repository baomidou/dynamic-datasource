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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * {@link DynamicDataSourceEndpoint} 的装配：仅在 actuator 2.2+ 存在时注册，endpoint 的启用/暴露
 * 完全交给 actuator 自身的 {@code management.endpoint.*} 配置（{@link ConditionalOnAvailableEndpoint}）。
 * <p>
 * 注意本类由三个 starter 的自动装配注册文件显式列出（Boot2 spring.factories、Boot3/4 imports）；
 * 类级 {@code @ConditionalOnClass} 以 ASM 方式评估，Boot 1.5 ~ 2.1 的 classpath 缺失新注解类时整体跳过。
 */
@Configuration
@ConditionalOnClass({Endpoint.class, ConditionalOnAvailableEndpoint.class})
public class DynamicDataSourceEndpointAutoConfiguration {

    @Bean
    @ConditionalOnAvailableEndpoint(endpoint = DynamicDataSourceEndpoint.class)
    @ConditionalOnMissingBean
    public DynamicDataSourceEndpoint dynamicDataSourceEndpoint(ObjectProvider<DataSource> dataSources,
                                                               DynamicDataSourceProperties properties) {
        return new DynamicDataSourceEndpoint(dataSources, properties);
    }
}
