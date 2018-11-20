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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.hikari;

import com.zaxxer.hikari.HikariConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * HikariCp参数配置
 *
 * @author TaoYu
 * @since 2.4.1
 */
@Data
@Slf4j
public class HikariCpConfig {

    private static final long CONNECTION_TIMEOUT = SECONDS.toMillis(30);
    private static final long VALIDATION_TIMEOUT = SECONDS.toMillis(5);
    private static final long IDLE_TIMEOUT = MINUTES.toMillis(10);
    private static final long MAX_LIFETIME = MINUTES.toMillis(30);
    private static final int DEFAULT_POOL_SIZE = 10;

    private String username;
    private String password;
    private String driverClassName;
    private String jdbcUrl;
    private String poolName;

    private String catalog;
    private Long connectionTimeout;
    private Long validationTimeout;
    private Long idleTimeout;
    private Long leakDetectionThreshold;
    private Long maxLifetime;
    private Integer maxPoolSize;
    private Integer minIdle;

    private Long initializationFailTimeout;
    private String connectionInitSql;
    private String connectionTestQuery;
    private String dataSourceClassName;
    private String dataSourceJndiName;
    private String schema;
    private String transactionIsolationName;
    private Boolean isAutoCommit;
    private Boolean isReadOnly;
    private Boolean isIsolateInternalQueries;
    private Boolean isRegisterMbeans;
    private Boolean isAllowPoolSuspension;
    private Properties dataSourceProperties;
    private Properties healthCheckProperties;

    public HikariConfig toHikariConfig(HikariCpConfig globalConfig) {
        HikariConfig config = new com.zaxxer.hikari.HikariConfig();

        String tempSchema = schema == null ? globalConfig.getSchema() : schema;
        if (tempSchema != null) {
            try {
                Field schemaField = com.zaxxer.hikari.HikariConfig.class.getDeclaredField("schema");
                schemaField.setAccessible(true);
                schemaField.set(config, tempSchema);
            } catch (NoSuchFieldException e) {
                log.warn("动态数据源-设置了Hikari的schema属性，但当前Hikari版本不支持");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        String tempCatalog = catalog == null ? globalConfig.getCatalog() : catalog;
        if (tempCatalog != null) {
            config.setCatalog(tempCatalog);
        }

        Long tempConnectionTimeout = connectionTimeout == null ? globalConfig.getConnectionTimeout() : connectionTimeout;
        if (tempConnectionTimeout != null && !tempConnectionTimeout.equals(CONNECTION_TIMEOUT)) {
            config.setConnectionTimeout(tempConnectionTimeout);
        }

        Long tempValidationTimeout = validationTimeout == null ? globalConfig.getValidationTimeout() : validationTimeout;
        if (tempValidationTimeout != null && !tempValidationTimeout.equals(VALIDATION_TIMEOUT)) {
            config.setValidationTimeout(tempValidationTimeout);
        }

        Long tempIdleTimeout = idleTimeout == null ? globalConfig.getIdleTimeout() : idleTimeout;
        if (tempIdleTimeout != null && !tempIdleTimeout.equals(IDLE_TIMEOUT)) {
            config.setIdleTimeout(tempIdleTimeout);
        }

        Long tempLeakDetectionThreshold = leakDetectionThreshold == null ? globalConfig.getLeakDetectionThreshold() : leakDetectionThreshold;
        if (tempLeakDetectionThreshold != null) {
            config.setLeakDetectionThreshold(tempLeakDetectionThreshold);
        }

        Long tempMaxLifetime = maxLifetime == null ? globalConfig.getMaxLifetime() : maxLifetime;
        if (tempMaxLifetime != null && !tempMaxLifetime.equals(MAX_LIFETIME)) {
            config.setMaxLifetime(tempMaxLifetime);
        }

        Integer tempMaxPoolSize = maxPoolSize == null ? globalConfig.getMaxPoolSize() : maxPoolSize;
        if (tempMaxPoolSize != null && !tempMaxPoolSize.equals(-1)) {
            config.setMaximumPoolSize(tempMaxPoolSize);
        }

        Integer tempMinIdle = minIdle == null ? globalConfig.getMinIdle() : getMinIdle();
        if (tempMinIdle != null && !tempMinIdle.equals(-1)) {
            config.setMinimumIdle(tempMinIdle);
        }

        Long tempInitializationFailTimeout = initializationFailTimeout == null ? globalConfig.getInitializationFailTimeout() : initializationFailTimeout;
        if (tempInitializationFailTimeout != null && !tempInitializationFailTimeout.equals(1L)) {
            config.setInitializationFailTimeout(tempInitializationFailTimeout);
        }

        String tempConnectionInitSql = connectionInitSql == null ? globalConfig.getConnectionInitSql() : connectionInitSql;
        if (tempConnectionInitSql != null) {
            config.setConnectionInitSql(tempConnectionInitSql);
        }

        String tempConnectionTestQuery = connectionTestQuery == null ? globalConfig.getConnectionTestQuery() : connectionTestQuery;
        if (tempConnectionTestQuery != null) {
            config.setConnectionTestQuery(tempConnectionTestQuery);
        }

        String tempDataSourceClassName = dataSourceClassName == null ? globalConfig.getDataSourceClassName() : dataSourceClassName;
        if (tempDataSourceClassName != null) {
            config.setDataSourceClassName(tempDataSourceClassName);
        }

        String tempDataSourceJndiName = dataSourceJndiName == null ? globalConfig.getDataSourceJndiName() : dataSourceJndiName;
        if (tempDataSourceJndiName != null) {
            config.setDataSourceJNDI(tempDataSourceJndiName);
        }

        String tempTransactionIsolationName = transactionIsolationName == null ? globalConfig.getTransactionIsolationName() : transactionIsolationName;
        if (tempTransactionIsolationName != null) {
            config.setTransactionIsolation(tempTransactionIsolationName);
        }

        Boolean tempAutoCommit = isAutoCommit == null ? globalConfig.getIsAutoCommit() : isAutoCommit;
        if (tempAutoCommit != null && tempAutoCommit.equals(Boolean.FALSE)) {
            config.setAutoCommit(false);
        }

        Boolean tempReadOnly = isReadOnly == null ? globalConfig.getIsReadOnly() : isReadOnly;
        if (tempReadOnly != null) {
            config.setReadOnly(tempReadOnly);
        }

        Boolean tempIsolateInternalQueries = isIsolateInternalQueries == null ? globalConfig.getIsIsolateInternalQueries() : isIsolateInternalQueries;
        if (tempIsolateInternalQueries != null) {
            config.setIsolateInternalQueries(tempIsolateInternalQueries);
        }

        Boolean tempRegisterMbeans = isRegisterMbeans == null ? globalConfig.getIsRegisterMbeans() : isRegisterMbeans;
        if (tempRegisterMbeans != null) {
            config.setRegisterMbeans(tempRegisterMbeans);
        }

        Boolean tempAllowPoolSuspension = isAllowPoolSuspension == null ? globalConfig.getIsAllowPoolSuspension() : isAllowPoolSuspension;
        if (tempAllowPoolSuspension != null) {
            config.setAllowPoolSuspension(tempAllowPoolSuspension);
        }

        Properties tempDataSourceProperties = dataSourceProperties == null ? globalConfig.getDataSourceProperties() : dataSourceProperties;
        if (tempDataSourceProperties != null) {
            config.setDataSourceProperties(tempDataSourceProperties);
        }

        Properties tempHealthCheckProperties = healthCheckProperties == null ? globalConfig.getHealthCheckProperties() : healthCheckProperties;
        if (tempHealthCheckProperties != null) {
            config.setHealthCheckProperties(tempHealthCheckProperties);
        }
        return config;
    }
}
