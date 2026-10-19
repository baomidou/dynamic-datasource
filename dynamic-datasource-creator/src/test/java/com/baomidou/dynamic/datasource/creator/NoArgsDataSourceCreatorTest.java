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
package com.baomidou.dynamic.datasource.creator;

import cn.beecp.BeeDataSource;
import com.baomidou.dynamic.datasource.creator.beecp.BeeCpDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.c3p0.C3p0DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.dbcp.Dbcp2DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.hikaricp.HikariDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.oracleucp.OracleUcpDataSourceCreator;
import com.baomidou.dynamic.datasource.toolkit.ConfigMergeCreator;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.zaxxer.hikari.HikariDataSource;
import oracle.ucp.jdbc.PoolDataSourceImpl;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用无参构造器创建的连接池数据源创建器没有全局配置，此前 {@code createDataSource} 总是在 {@link ConfigMergeCreator}
 * 里抛出 {@link NullPointerException}（#821）。现在与传入空的全局配置时一样，只应用单个数据源自己的连接池配置。
 * <p>
 * starter 总是传入全局配置，只有不经 Spring Boot、手动组装时才会用到无参构造器。每个用例只设置一项与连接池默认值不同的配置，
 * 用来确认这项配置确实作用到了数据源上。数据源都按懒加载创建，不会建立连接。
 */
class NoArgsDataSourceCreatorTest {

    @Test
    void hikariShouldApplyTheDatasourceConfig() {
        DataSourceProperty property = h2("no_args_hikari");
        property.getHikari().setMaximumPoolSize(5);

        DataSource dataSource = new HikariDataSourceCreator().createDataSource(property);

        assertThat(dataSource).isInstanceOfSatisfying(HikariDataSource.class,
                hikari -> assertThat(hikari.getMaximumPoolSize()).isEqualTo(5));
    }

    @Test
    void beeCpShouldApplyTheDatasourceConfig() {
        DataSourceProperty property = h2("no_args_beecp");
        property.getBeecp().setMaxActive(5);

        DataSource dataSource = new BeeCpDataSourceCreator().createDataSource(property);

        assertThat(dataSource).isInstanceOfSatisfying(BeeDataSource.class,
                beeCp -> assertThat(beeCp.getMaxActive()).isEqualTo(5));
    }

    @Test
    void dbcp2ShouldApplyTheDatasourceConfig() {
        DataSourceProperty property = h2("no_args_dbcp2");
        property.getDbcp2().setMaxTotal(5);

        DataSource dataSource = new Dbcp2DataSourceCreator().createDataSource(property);

        assertThat(dataSource).isInstanceOfSatisfying(BasicDataSource.class,
                dbcp2 -> assertThat(dbcp2.getMaxTotal()).isEqualTo(5));
    }

    @Test
    void c3p0ShouldApplyTheDatasourceConfig() {
        DataSourceProperty property = h2("no_args_c3p0");
        property.getC3p0().setMaxPoolSize(5);

        DataSource dataSource = new C3p0DataSourceCreator().createDataSource(property);

        assertThat(dataSource).isInstanceOfSatisfying(ComboPooledDataSource.class,
                c3p0 -> assertThat(c3p0.getMaxPoolSize()).isEqualTo(5));
    }

    @Test
    void oracleUcpShouldApplyTheDatasourceConfig() {
        DataSourceProperty property = h2("no_args_oracle_ucp");
        property.getOracleUcp().setMaxPoolSize(5);

        DataSource dataSource = new OracleUcpDataSourceCreator().createDataSource(property);

        assertThat(dataSource).isInstanceOfSatisfying(PoolDataSourceImpl.class,
                oracleUcp -> assertThat(oracleUcp.getMaxPoolSize()).isEqualTo(5));
    }

    private static DataSourceProperty h2(String name) {
        DataSourceProperty property = new DataSourceProperty();
        property.setPoolName(name);
        property.setUrl("jdbc:h2:mem:" + name);
        property.setUsername("sa");
        property.setPassword("");
        property.setDriverClassName("org.h2.Driver");
        return property;
    }
}
