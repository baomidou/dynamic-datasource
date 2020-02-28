/**
 * Copyright © 2020 organization humingfeng
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
package cn.humingfeng.dynamic.datasource.spring.boot.autoconfigure;

import cn.humingfeng.dynamic.datasource.creator.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AllArgsConstructor
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceCreatorAutoConfiguration {

  private final DynamicDataSourceProperties properties;

  @Bean
  @ConditionalOnMissingBean
  public DataSourceCreator dataSourceCreator() {
    DataSourceCreator dataSourceCreator = new DataSourceCreator();
    dataSourceCreator.setBasicDataSourceCreator(basicDataSourceCreator());
    dataSourceCreator.setGbaseDataSourceCreator(gbaseDataSourceCreator());
    dataSourceCreator.setJndiDataSourceCreator(jndiDataSourceCreator());
    dataSourceCreator.setDruidDataSourceCreator(druidDataSourceCreator());
    dataSourceCreator.setHikariDataSourceCreator(hikariDataSourceCreator());
    dataSourceCreator.setGlobalPublicKey(properties.getPublicKey());
    return dataSourceCreator;
  }

  @Bean
  @ConditionalOnMissingBean
  public BasicDataSourceCreator basicDataSourceCreator() {
    return new BasicDataSourceCreator();
  }

  @Bean
  @ConditionalOnMissingBean
  public GbaseDataSourceCreator gbaseDataSourceCreator() {
      return new GbaseDataSourceCreator();
  }

  @Bean
  @ConditionalOnMissingBean
  public JndiDataSourceCreator jndiDataSourceCreator() {
    return new JndiDataSourceCreator();
  }

  @Bean
  @ConditionalOnMissingBean
  public DruidDataSourceCreator druidDataSourceCreator() {
    return new DruidDataSourceCreator(properties.getDruid());
  }

  @Bean
  @ConditionalOnMissingBean
  public HikariDataSourceCreator hikariDataSourceCreator() {
    return new HikariDataSourceCreator(properties.getHikari());
  }
}
