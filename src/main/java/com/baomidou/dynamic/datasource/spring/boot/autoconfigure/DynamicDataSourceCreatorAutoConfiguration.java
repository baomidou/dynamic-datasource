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

import cn.beecp.BeeDataSource;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.creator.*;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author TaoYu
 */
@Configuration
public class DynamicDataSourceCreatorAutoConfiguration {

    public static final int JNDI_ORDER = 1000;
    public static final int DRUID_ORDER = 2000;
    public static final int HIKARI_ORDER = 3000;
    public static final int BEECP_ORDER = 4000;
    public static final int DBCP2_ORDER = 5000;
    public static final int DEFAULT_ORDER = 6000;

    @Primary
    @Bean
    @ConditionalOnMissingBean
    public DefaultDataSourceCreator dataSourceCreator(List<DataSourceCreator> dataSourceCreators) {
        DefaultDataSourceCreator defaultDataSourceCreator = new DefaultDataSourceCreator();
        defaultDataSourceCreator.setCreators(dataSourceCreators);
        return defaultDataSourceCreator;
    }

    @Bean
    @Order(DEFAULT_ORDER)
    public BasicDataSourceCreator basicDataSourceCreator() {
        return new BasicDataSourceCreator();
    }

    @Bean
    @Order(JNDI_ORDER)
    public JndiDataSourceCreator jndiDataSourceCreator() {
        return new JndiDataSourceCreator();
    }

    /**
     * 存在Druid数据源时, 加入创建器
     */
    @ConditionalOnClass(DruidDataSource.class)
    @Configuration
    static class DruidDataSourceCreatorConfiguration {

        @Bean
        @Order(DRUID_ORDER)
        public DruidDataSourceCreator druidDataSourceCreator() {
            return new DruidDataSourceCreator();
        }
    }

    /**
     * 存在Hikari数据源时, 加入创建器
     */
    @ConditionalOnClass(HikariDataSource.class)
    @Configuration
    static class HikariDataSourceCreatorConfiguration {
        @Bean
        @Order(HIKARI_ORDER)
        public HikariDataSourceCreator hikariDataSourceCreator() {
            return new HikariDataSourceCreator();
        }
    }

    /**
     * 存在BeeCp数据源时, 加入创建器
     */
    @ConditionalOnClass(BeeDataSource.class)
    @Configuration
    static class BeeCpDataSourceCreatorConfiguration {

        @Bean
        @Order(BEECP_ORDER)
        public BeeCpDataSourceCreator beeCpDataSourceCreator() {
            return new BeeCpDataSourceCreator();
        }
    }

    /**
     * 存在Dbcp2数据源时, 加入创建器
     */
    @ConditionalOnClass(BasicDataSource.class)
    @Configuration
    static class Dbcp2DataSourceCreatorConfiguration {

        @Bean
        @Order(DBCP2_ORDER)
        public Dbcp2DataSourceCreator dbcp2DataSourceCreator() {
            return new Dbcp2DataSourceCreator();
        }
    }

}
