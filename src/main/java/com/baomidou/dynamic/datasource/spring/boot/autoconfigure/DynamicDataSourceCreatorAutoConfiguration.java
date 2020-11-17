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

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.creator.*;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author TaoYu
 */
@Slf4j
@Configuration
@AllArgsConstructor
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceCreatorAutoConfiguration {

    private static final int JNDI_ORDER = 1000;
    private static final int DRUID_ORDER = 2000;
    private static final int HIKARI_ORDER = 3000;
    private static final int DEFAULT_ORDER = 5000;
    private final DynamicDataSourceProperties properties;

    @Primary
    @Bean
    @ConditionalOnMissingBean
    public DefaultDataSourceCreator dataSourceCreator(List<DataSourceCreator> dataSourceCreators) {
        DefaultDataSourceCreator defaultDataSourceCreator = new DefaultDataSourceCreator();
        defaultDataSourceCreator.setProperties(properties);
        defaultDataSourceCreator.setDataSourceCreators(dataSourceCreators);
        return defaultDataSourceCreator;
    }

    @Bean
    @Order(DEFAULT_ORDER)
    @ConditionalOnMissingBean
    public BasicDataSourceCreator basicDataSourceCreator() {
        return new BasicDataSourceCreator();
    }

    @Bean
    @Order(JNDI_ORDER)
    @ConditionalOnMissingBean
    public JndiDataSourceCreator jndiDataSourceCreator() {
        return new JndiDataSourceCreator();
    }

    /**
     * 存在Druid数据源时, 加入创建器
     */
    @ConditionalOnClass(DruidDataSource.class)
    @Configuration
    public class DruidDataSourceCreatorConfiguration {
        @Bean
        @Order(DRUID_ORDER)
        @ConditionalOnMissingBean
        public DruidDataSourceCreator druidDataSourceCreator() {
            return new DruidDataSourceCreator(properties.getDruid());
        }

    }

    /**
     * 存在Hikari数据源时, 加入创建器
     */
    @ConditionalOnClass(HikariDataSource.class)
    @Configuration
    public class HikariDataSourceCreatorConfiguration {
        @Bean
        @Order(HIKARI_ORDER)
        @ConditionalOnMissingBean
        public HikariDataSourceCreator hikariDataSourceCreator() {
            return new HikariDataSourceCreator(properties.getHikari());
        }
    }

}
