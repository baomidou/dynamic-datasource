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
package com.baomidou.dynamic.datasource.creator;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.hikari.HikariCpConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.baomidou.dynamic.datasource.support.DdConstants.HIKARI_DATASOURCE;

/**
 * Hikari数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/21
 */
@Data
@AllArgsConstructor
public class HikariDataSourceCreator extends AbstractDataSourceCreator implements DataSourceCreator {

    private static Boolean hikariExists = false;
    private static Method configCopyMethod = null;

    static {

        try {
            Class.forName(HIKARI_DATASOURCE);
            hikariExists = true;
            fetchMethod();
        } catch (ClassNotFoundException ignored) {
        }
    }

    private HikariCpConfig hikariCpConfig;

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty, String publicKey) {
        if (StringUtils.isEmpty(dataSourceProperty.getPublicKey())) {
            dataSourceProperty.setPublicKey(publicKey);
        }
        HikariConfig config = dataSourceProperty.getHikari().toHikariConfig(hikariCpConfig);
        config.setUsername(dataSourceProperty.getUsername());
        config.setPassword(dataSourceProperty.getPassword());
        config.setJdbcUrl(dataSourceProperty.getUrl());
        config.setPoolName(dataSourceProperty.getPoolName());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (!StringUtils.isEmpty(driverClassName)) {
            config.setDriverClassName(driverClassName);
        }
        config.validate();
        HikariDataSource dataSource = new HikariDataSource();
        try {
            configCopyMethod.invoke(config, dataSource);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("HikariConfig failed to copy to HikariDataSource", e);
        }
        return dataSource;
    }


    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return (type == null && hikariExists) || (type != null && HIKARI_DATASOURCE.equals(type.getName()));
    }

    /**
     * to support springboot 1.5 and 2.x
     * HikariConfig 2.x use 'copyState' to copy config
     * HikariConfig 3.x use 'copyStateTo' to copy config
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    private static void fetchMethod() {
        try {
            configCopyMethod = HikariConfig.class.getMethod("copyState", HikariConfig.class);
            return;
        } catch (NoSuchMethodException ignored) {
        }

        try {
            configCopyMethod = HikariConfig.class.getMethod("copyStateTo", HikariConfig.class);
            return;
        } catch (NoSuchMethodException ignored) {
        }

        throw new RuntimeException("HikariConfig does not has 'copyState' or 'copyStateTo' method!");
    }
}
