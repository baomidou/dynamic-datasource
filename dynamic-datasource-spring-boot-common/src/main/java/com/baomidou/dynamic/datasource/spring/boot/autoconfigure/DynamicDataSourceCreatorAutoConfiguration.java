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
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.baomidou.dynamic.datasource.creator.atomikos.AtomikosDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.basic.BasicDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.beecp.BeeCpDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.c3p0.C3p0DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.dbcp.Dbcp2DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.druid.DruidConfig;
import com.baomidou.dynamic.datasource.creator.druid.DruidDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.jndi.JndiDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.oracleucp.OracleUcpDataSourceCreator;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import com.baomidou.dynamic.datasource.tx.AtomikosTransactionFactory;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.transaction.TransactionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.ArrayList;
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
    public static final int ATOMIKOS_ORDER = 6000;
    public static final int C3P0_ORDER = 7000;
    public static final int ORACLE_UCP_ORDER = 8000;
    public static final int DEFAULT_ORDER = 9000;

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
    @Slf4j
    static class DruidDataSourceCreatorConfiguration {

        @Autowired(required = false)
        private ApplicationContext applicationContext;

        @Bean
        @Order(DRUID_ORDER)
        public DruidDataSourceCreator druidDataSourceCreator(DynamicDataSourceProperties properties) {
            DruidConfig druid = properties.getDruid();
            return new DruidDataSourceCreator(druid, proxyFilters -> {
                List<Filter> filters = new ArrayList<>();
                if (applicationContext != null && DsStrUtils.hasText(proxyFilters)) {
                    for (String filterId : proxyFilters.split(",")) {
                        try {
                            filters.add(applicationContext.getBean(filterId, Filter.class));
                        } catch (Exception e) {
                            log.warn("dynamic-datasource cannot load druid filter with name [{}], will be ignored", filterId);
                        }
                    }
                }
                return filters;
            });
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
        public HikariDataSourceCreator hikariDataSourceCreator(DynamicDataSourceProperties properties) {
            return new HikariDataSourceCreator(properties.getHikari());
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
        public BeeCpDataSourceCreator beeCpDataSourceCreator(DynamicDataSourceProperties properties) {
            return new BeeCpDataSourceCreator(properties.getBeecp());
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
        public Dbcp2DataSourceCreator dbcp2DataSourceCreator(DynamicDataSourceProperties properties) {
            return new Dbcp2DataSourceCreator(properties.getDbcp2());
        }

    }

    /**
     * 存在Atomikos数据源时, 加入创建器
     */
    @ConditionalOnClass({AtomikosDataSourceBean.class, TransactionFactory.class})
    @Configuration
    static class AtomikosDataSourceCreatorConfiguration {

        @Bean
        @Order(ATOMIKOS_ORDER)
        public AtomikosDataSourceCreator atomikosDataSourceCreator(DynamicDataSourceProperties properties) {
            return new AtomikosDataSourceCreator(properties.getAtomikos());
        }

        @Bean
        public TransactionFactory atomikosTransactionFactory() {
            return new AtomikosTransactionFactory();
        }

    }

    @ConditionalOnClass({ComboPooledDataSource.class})
    @Configuration
    static class C3p0DataSourceCreatorConfiguration {
        @Bean
        @Order(C3P0_ORDER)
        public C3p0DataSourceCreator c3p0DataSourceCreator(DynamicDataSourceProperties properties) {
            return new C3p0DataSourceCreator(properties.getC3p0());
        }
    }

    @ConditionalOnClass({PoolDataSourceImpl.class})
    @Configuration
    static class OracleUcp0DataSourceCreatorConfiguration {
        @Bean
        @Order(ORACLE_UCP_ORDER)
        public OracleUcpDataSourceCreator oracleUcpDataSourceCreator(DynamicDataSourceProperties properties) {
            return new OracleUcpDataSourceCreator(properties.getOracleUcp());
        }
    }
}