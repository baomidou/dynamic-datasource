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
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.atomikos.AtomikosDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.basic.BasicDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.beecp.BeeCpDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.dbcp.Dbcp2DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.druid.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.jndi.JndiDataSourceCreator;
import com.baomidou.dynamic.datasource.tx.AtomikosTransactionFactory;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.transaction.TransactionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author TaoYu
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class DynamicDataSourceCreatorAutoConfiguration {

    public static final int JNDI_ORDER = 1000;
    public static final int DRUID_ORDER = 2000;
    public static final int HIKARI_ORDER = 3000;
    public static final int BEECP_ORDER = 4000;
    public static final int DBCP2_ORDER = 5000;
    public static final int ATOMIKOS_ORDER = 6000;
    public static final int DEFAULT_ORDER = 7000;
    private final DynamicDataSourceProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public DefaultDataSourceCreator dataSourceCreator(List<DataSourceCreator> dataSourceCreators) {
        DefaultDataSourceCreator creator = new DefaultDataSourceCreator();
        creator.setCreators(dataSourceCreators);
        return creator;
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

    @Bean
    @Order(DRUID_ORDER)
    @ConditionalOnClass(DruidDataSource.class)
    public DruidDataSourceCreator druidDataSourceCreator() {
        return new DruidDataSourceCreator(properties.getDruid());
    }

    /**
     * 存在Hikari数据源时, 加入创建器
     */
    @Bean
    @Order(HIKARI_ORDER)
    @ConditionalOnClass(HikariDataSource.class)
    public HikariDataSourceCreator hikariDataSourceCreator() {
        return new HikariDataSourceCreator(properties.getHikari());
    }

    /**
     * 存在BeeCp数据源时, 加入创建器
     */
    @Bean
    @Order(BEECP_ORDER)
    @ConditionalOnClass(BeeDataSource.class)
    public BeeCpDataSourceCreator beeCpDataSourceCreator() {
        return new BeeCpDataSourceCreator(properties.getBeecp());
    }

    /**
     * 存在Dbcp2数据源时, 加入创建器
     */
    @Bean
    @Order(DBCP2_ORDER)
    @ConditionalOnClass(BasicDataSource.class)
    public Dbcp2DataSourceCreator dbcp2DataSourceCreator() {
        return new Dbcp2DataSourceCreator(properties.getDbcp2());
    }

    /**
     * 存在Atomikos数据源时, 加入创建器
     */
    @Bean
    @Order(ATOMIKOS_ORDER)
    @ConditionalOnClass({AtomikosDataSourceBean.class})
    public AtomikosDataSourceCreator atomikosDataSourceCreator() {
        return new AtomikosDataSourceCreator(properties.getAtomikos());
    }

    @Bean
    @ConditionalOnClass({AtomikosDataSourceBean.class, TransactionFactory.class})
    public TransactionFactory atomikosTransactionFactory() {
        return new AtomikosTransactionFactory();
    }

}