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

import com.baomidou.dynamic.datasource.DynamicDataSourceConfigure;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAdvisor;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationAdvisor;
import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationInterceptor;
import com.baomidou.dynamic.datasource.processor.DsHeaderProcessor;
import com.baomidou.dynamic.datasource.processor.DsProcessor;
import com.baomidou.dynamic.datasource.processor.DsSessionProcessor;
import com.baomidou.dynamic.datasource.processor.DsSpelExpressionProcessor;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.provider.YmlDynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidDynamicDataSourceConfiguration;
import com.baomidou.dynamic.datasource.strategy.DynamicDataSourceStrategy;
import java.util.Map;
import javax.sql.DataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

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
    dataSource.setStrategy(properties.getStrategy());
    dataSource.setProvider(dynamicDataSourceProvider);
    dataSource.setP6spy(properties.getP6spy());
    dataSource.setStrict(properties.getStrict());
    dataSource.setSeata(properties.getSeata());
    return dataSource;
  }

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
  @ConditionalOnBean(DynamicDataSourceConfigure.class)
  public DynamicDataSourceAdvisor dynamicAdvisor(DynamicDataSourceConfigure dynamicDataSourceConfigure, DsProcessor dsProcessor) {
    DynamicDataSourceAdvisor advisor = new DynamicDataSourceAdvisor(dynamicDataSourceConfigure.getMatchers());
    advisor.setDsProcessor(dsProcessor);
    advisor.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return advisor;
  }
}
