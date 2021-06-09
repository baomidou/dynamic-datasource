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
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationAdvisor;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationInterceptor;
import com.baomidou.dynamic.datasource.aop.DynamicLocalTransactionAdvisor;
import com.baomidou.dynamic.datasource.processor.DsHeaderProcessor;
import com.baomidou.dynamic.datasource.processor.DsProcessor;
import com.baomidou.dynamic.datasource.processor.DsSessionProcessor;
import com.baomidou.dynamic.datasource.processor.DsSpelExpressionProcessor;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.YmlDynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidDynamicDataSourceConfiguration;
import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * 动态数据源核心自动配置类
 *
 * @author TaoYu Kanyuxia
 * @see DynamicDataSourceProvider
 * @see DynamicDataSourceStrategy
 * @see DynamicRoutingDataSource
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@AutoConfigureBefore(value = DataSourceAutoConfiguration.class, name = "com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure")
@Import(value = {DruidDynamicDataSourceConfiguration.class, DynamicDataSourceCreatorAutoConfiguration.class, DynamicDataSourceHealthCheckConfiguration.class})
@ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicDataSourceAutoConfiguration implements InitializingBean {

    private final DynamicDataSourceProperties properties;

    private final List<DynamicDataSourcePropertiesCustomizer> dataSourcePropertiesCustomizers;

    public DynamicDataSourceAutoConfiguration(
            DynamicDataSourceProperties properties,
            ObjectProvider<List<DynamicDataSourcePropertiesCustomizer>> dataSourcePropertiesCustomizers) {
        this.properties = properties;
        this.dataSourcePropertiesCustomizers = dataSourcePropertiesCustomizers.getIfAvailable();
    }

    @Bean
    public DynamicDataSourceProvider ymlDynamicDataSourceProvider() {
        return new YmlDynamicDataSourceProvider(properties.getDatasource());
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource() {
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        dataSource.setPrimary(properties.getPrimary());
        dataSource.setStrict(properties.getStrict());
        dataSource.setStrategy(properties.getStrategy());
        dataSource.setP6spy(properties.getP6spy());
        dataSource.setSeata(properties.getSeata());
        return dataSource;
    }

    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    public Advisor dynamicDatasourceAnnotationAdvisor(DsProcessor dsProcessor) {
        DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor(properties.isAllowedPublicOnly(), dsProcessor);
        DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor);
        advisor.setOrder(properties.getOrder());
        return advisor;
    }

    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "seata", havingValue = "false", matchIfMissing = true)
    @Bean
    public Advisor dynamicTransactionAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("@annotation(com.baomidou.dynamic.datasource.annotation.DSTransactional)");
        return new DefaultPointcutAdvisor(pointcut, new DynamicLocalTransactionAdvisor());
    }

    @Bean
    @ConditionalOnMissingBean
    public DsProcessor dsProcessor(BeanFactory beanFactory) {
        DsHeaderProcessor headerProcessor = new DsHeaderProcessor();
        DsSessionProcessor sessionProcessor = new DsSessionProcessor();
        DsSpelExpressionProcessor spelExpressionProcessor = new DsSpelExpressionProcessor();
        spelExpressionProcessor.setBeanResolver(new BeanFactoryResolver(beanFactory));
        headerProcessor.setNextProcessor(sessionProcessor);
        sessionProcessor.setNextProcessor(spelExpressionProcessor);
        return headerProcessor;
    }

    @Override
    public void afterPropertiesSet() {
        if (!CollectionUtils.isEmpty(dataSourcePropertiesCustomizers)) {
            for (DynamicDataSourcePropertiesCustomizer customizer : dataSourcePropertiesCustomizers) {
                customizer.customize(properties);
            }
        }
    }

}
