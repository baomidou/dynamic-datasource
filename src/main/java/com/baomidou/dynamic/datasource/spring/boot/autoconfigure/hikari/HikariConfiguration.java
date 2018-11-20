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

import com.zaxxer.hikari.HikariConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * HikariCp配置文件类
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

    public HikariConfig toHikariConfig(HikariCpConfig config) {
        HikariConfig configuration = new HikariConfig();

        String tempSchema = schema == null ? config.getSchema() : schema;
        if (tempSchema != null) {
            try {
                Field schemaField = HikariConfig.class.getDeclaredField("schema");
                schemaField.setAccessible(true);
                schemaField.set(configuration, tempSchema);
            } catch (NoSuchFieldException e) {
                log.warn("动态数据源-设置了Hikari的schema属性，但当前Hikari版本不支持");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        String tempCatalog = catalog == null ? config.getCatalog() : catalog;
        if (tempCatalog != null) {
            configuration.setCatalog(tempCatalog);
        }

        Long tempConnectionTimeout = connectionTimeout == null ? config.getConnectionTimeout() : connectionTimeout;
        if (tempConnectionTimeout != null && !tempConnectionTimeout.equals(CONNECTION_TIMEOUT)) {
            configuration.setConnectionTimeout(tempConnectionTimeout);
        }

        Long tempValidationTimeout = validationTimeout == null ? config.getValidationTimeout() : validationTimeout;
        if (tempValidationTimeout != null && !tempValidationTimeout.equals(VALIDATION_TIMEOUT)) {
            configuration.setValidationTimeout(tempValidationTimeout);
        }

        Long tempIdleTimeout = idleTimeout == null ? config.getIdleTimeout() : idleTimeout;
        if (tempIdleTimeout != null && !tempIdleTimeout.equals(IDLE_TIMEOUT)) {
            configuration.setIdleTimeout(tempIdleTimeout);
        }

        Long tempLeakDetectionThreshold = leakDetectionThreshold == null ? config.getLeakDetectionThreshold() : leakDetectionThreshold;
        if (tempLeakDetectionThreshold != null) {
            configuration.setLeakDetectionThreshold(tempLeakDetectionThreshold);
        }

        Long tempMaxLifetime = maxLifetime == null ? config.getMaxLifetime() : maxLifetime;
        if (tempMaxLifetime != null && !tempMaxLifetime.equals(MAX_LIFETIME)) {
            configuration.setMaxLifetime(tempMaxLifetime);
        }

        Integer tempMaxPoolSize = maxPoolSize == null ? config.getMaxPoolSize() : maxPoolSize;
        if (tempMaxPoolSize != null && !tempMaxPoolSize.equals(-1)) {
            configuration.setMaximumPoolSize(tempMaxPoolSize);
        }

        Integer tempMinIdle = minIdle == null ? config.getMinIdle() : getMinIdle();
        if (tempMinIdle != null && !tempMinIdle.equals(-1)) {
            configuration.setMinimumIdle(tempMinIdle);
        }

        Long tempInitializationFailTimeout = initializationFailTimeout == null ? config.getInitializationFailTimeout() : initializationFailTimeout;
        if (tempInitializationFailTimeout != null && !tempInitializationFailTimeout.equals(1)) {
            configuration.setInitializationFailTimeout(tempInitializationFailTimeout);
        }

        String tempConnectionInitSql = connectionInitSql == null ? config.getConnectionInitSql() : connectionInitSql;
        if (tempConnectionInitSql != null) {
            configuration.setConnectionInitSql(tempConnectionInitSql);
        }

        String tempConnectionTestQuery = connectionTestQuery == null ? config.getConnectionTestQuery() : connectionTestQuery;
        if (tempConnectionTestQuery != null) {
            configuration.setConnectionTestQuery(tempConnectionTestQuery);
        }

        String tempDataSourceClassName = dataSourceClassName == null ? config.getDataSourceClassName() : dataSourceClassName;
        if (tempDataSourceClassName != null) {
            configuration.setDataSourceClassName(tempDataSourceClassName);
        }

        String tempDataSourceJndiName = dataSourceJndiName == null ? config.getDataSourceJndiName() : dataSourceJndiName;
        if (tempDataSourceJndiName != null) {
            configuration.setDataSourceJNDI(tempDataSourceJndiName);
        }

        String tempTransactionIsolationName = transactionIsolationName == null ? config.getTransactionIsolationName() : transactionIsolationName;
        if (tempTransactionIsolationName != null) {
            configuration.setTransactionIsolation(tempTransactionIsolationName);
        }

        Boolean tempAutoCommit = isAutoCommit == null ? config.getIsAutoCommit() : isAutoCommit;
        if (tempAutoCommit != null && tempAutoCommit.equals(Boolean.FALSE)) {
            configuration.setAutoCommit(false);
        }

        Boolean tempReadOnly = isReadOnly == null ? config.getIsReadOnly() : isReadOnly;
        if (tempReadOnly != null) {
            configuration.setReadOnly(tempReadOnly);
        }

        Boolean tempIsolateInternalQueries = isIsolateInternalQueries == null ? config.getIsIsolateInternalQueries() : isIsolateInternalQueries;
        if (tempIsolateInternalQueries != null) {
            configuration.setIsolateInternalQueries(tempIsolateInternalQueries);
        }

        Boolean tempRegisterMbeans = isRegisterMbeans == null ? config.getIsRegisterMbeans() : isRegisterMbeans;
        if (tempRegisterMbeans != null) {
            configuration.setRegisterMbeans(tempRegisterMbeans);
        }

        Boolean tempAllowPoolSuspension = isAllowPoolSuspension == null ? config.getIsAllowPoolSuspension() : isAllowPoolSuspension;
        if (tempAllowPoolSuspension != null) {
            configuration.setAllowPoolSuspension(tempAllowPoolSuspension);
        }

        Properties tempDataSourceProperties = dataSourceProperties == null ? config.getDataSourceProperties() : dataSourceProperties;
        if (tempDataSourceProperties != null) {
            configuration.setDataSourceProperties(tempDataSourceProperties);
        }

        Properties tempHealthCheckProperties = healthCheckProperties == null ? config.getHealthCheckProperties() : healthCheckProperties;
        if (tempHealthCheckProperties != null) {
            configuration.setHealthCheckProperties(tempHealthCheckProperties);
        }
        return configuration;
    }
}
