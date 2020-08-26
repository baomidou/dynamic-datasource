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

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.TestSource;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationAdvisor;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationInterceptor;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.processor.DsHeaderProcessor;
import com.baomidou.dynamic.datasource.processor.DsProcessor;
import com.baomidou.dynamic.datasource.processor.DsSessionProcessor;
import com.baomidou.dynamic.datasource.processor.DsSpelExpressionProcessor;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.YmlDynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidDynamicDataSourceConfiguration;
import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.autoproxy.InfrastructureAdvisorAutoProxyCreator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import org.springframework.transaction.interceptor.AbstractFallbackTransactionAttributeSource;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
@AllArgsConstructor
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@Import(value = {DruidDynamicDataSourceConfiguration.class, DynamicDataSourceCreatorAutoConfiguration.class})
@ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicDataSourceAutoConfiguration {

    private final DynamicDataSourceProperties properties;

    private static final String TRANSACTION_MANAGER_SUFFIX = "TransactionManager";

    @Autowired
    private ApplicationContext applicationContext;




    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceProvider dynamicDataSourceProvider() {
        Map<String, DataSourceProperty> datasourceMap = properties.getDatasource();
        return new YmlDynamicDataSourceProvider(datasourceMap);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource(DynamicDataSourceProvider dynamicDataSourceProvider) {
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        dataSource.setPrimary(properties.getPrimary());
        dataSource.setStrict(properties.getStrict());
        dataSource.setStrategy(properties.getStrategy());
        dataSource.setProvider(dynamicDataSourceProvider);
        dataSource.setP6spy(properties.getP6spy());
        dataSource.setSeata(properties.getSeata());
        //动态注入多个transactionManager
        //有多少个datasource 就有多少个 transactionManager
        //默认第一个为primary
        //!!!!!!! 注意 !!!!!!!!!
        //这里有一个奇葩东西 我获取DynamicRoutingDataSource里面的ItemDataSource还不能回滚 必须用DynamicRoutingDataSource
        addTransactionManager(dynamicDataSourceProvider,dataSource);
        return dataSource;
    }



    @Role(value = BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    @ConditionalOnMissingBean
    public DynamicDataSourceAnnotationAdvisor dynamicDatasourceAnnotationAdvisor(DsProcessor dsProcessor) {
        DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor();
        interceptor.setDsProcessor(dsProcessor);
        DynamicDataSourceAnnotationAdvisor advisor = new DynamicDataSourceAnnotationAdvisor(interceptor);
        advisor.setOrder(properties.getOrder());
        return advisor;
    }


    @Bean
    @ConditionalOnMissingBean
    public DsProcessor dsProcessor() {
        DsHeaderProcessor headerProcessor = new DsHeaderProcessor();
        DsSessionProcessor sessionProcessor = new DsSessionProcessor();
        DsSpelExpressionProcessor spelExpressionProcessor = new DsSpelExpressionProcessor();
        headerProcessor.setNextProcessor(sessionProcessor);
        sessionProcessor.setNextProcessor(spelExpressionProcessor);
        return headerProcessor;
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicTransactionProcessor getDynamicTransactionProcessor() {
        return new DynamicTransactionProcessor();
    }



    private void addTransactionManager(DynamicDataSourceProvider dynamicDataSourceProvider,DynamicRoutingDataSource dataSource) {
        if (CollectionUtils.isNotEmpty(dynamicDataSourceProvider.loadDataSources())) {
            int i=0;
            for (Map.Entry<String, DataSource> entry : dynamicDataSourceProvider.loadDataSources().entrySet()) {
                if (i==0){
                    doRegisterBean(dataSource, entry, true);
                }else {
                    doRegisterBean(dataSource, entry, false);
                }
                i++;
            }
        }
    }

    private void doRegisterBean(DynamicRoutingDataSource dataSource, Map.Entry<String, DataSource> entry, boolean b) {
        dataSource.setPrimary(entry.getKey());

        registerBean(entry.getKey() + TRANSACTION_MANAGER_SUFFIX,
                DataSourceTransactionManager.class,
                (ConfigurableApplicationContext) applicationContext,
                b,
                entry.getValue());
    }

    /**
     * 动态注册bean
     *
     * @param name               bean 名称
     * @param clazz              bean class
     * @param applicationContext 上下文
     * @param args               参数
     * @param <T>
     * @return
     */
    private <T> T registerBean(String name, Class<T> clazz, ConfigurableApplicationContext applicationContext,boolean flag, Object... args) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz);
        if (args.length > 0) {
            for (Object arg : args) {
                beanDefinitionBuilder.addConstructorArgValue(arg);
            }
        }

        BeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        if (flag){
            beanDefinition.setPrimary(true);
        }
        BeanDefinitionRegistry beanFactory = (BeanDefinitionRegistry) applicationContext.getBeanFactory();

        beanFactory.registerBeanDefinition(name, beanDefinition);
        return applicationContext.getBean(name, clazz);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public InfrastructureAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        return new InfrastructureAdvisorAutoProxyCreator();
    }



}
