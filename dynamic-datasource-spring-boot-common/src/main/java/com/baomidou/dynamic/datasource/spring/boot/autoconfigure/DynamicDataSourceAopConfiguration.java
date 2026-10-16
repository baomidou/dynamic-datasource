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

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationAdvisor;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationInterceptor;
import com.baomidou.dynamic.datasource.aop.DynamicLocalTransactionInterceptor;
import com.baomidou.dynamic.datasource.processor.DsProcessor;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * 动态数据源 AOP 自动配置
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceAopConfiguration {

    private final DynamicDataSourceProperties properties;

    public DynamicDataSourceAopConfiguration(DynamicDataSourceProperties properties) {
        this.properties = properties;
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    @ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX + ".aop", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Advisor dynamicDatasourceAnnotationAdvisor(DsProcessor dsProcessor) {
        DynamicDatasourceAopProperties aopProperties = properties.getAop();
        DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor(aopProperties.getAllowedPublicOnly(), dsProcessor);
        DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor, DS.class);
        advisor.setOrder(aopProperties.getOrder());
        return advisor;
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    @ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "seata", havingValue = "false", matchIfMissing = true)
    public Advisor dynamicTransactionAdvisor() {
        DynamicDatasourceAopProperties aopProperties = properties.getAop();
        DynamicLocalTransactionInterceptor interceptor = new DynamicLocalTransactionInterceptor(aopProperties.getAllowedPublicOnly());
        return new DynamicDataSourceAnnotationAdvisor(interceptor, DSTransactional.class);
    }
}
