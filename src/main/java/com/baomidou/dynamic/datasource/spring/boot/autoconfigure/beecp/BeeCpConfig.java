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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.beecp;

import cn.beecp.BeeDataSourceConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * BeeCp参数配置
 *
 * @author TaoYu
 * @since 3.3.4
 */
@Data
@Slf4j
public class BeeCpConfig {

    private String defaultCatalog;
    private String defaultSchema;
    private Boolean defaultReadOnly;
    private Boolean defaultAutoCommit;
    private Integer defaultTransactionIsolationCode;
    private String defaultTransactionIsolation;

    private Boolean fairMode;
    private Integer initialSize;
    private Integer maxActive;
    private Integer borrowSemaphoreSize;
    private Long maxWait;
    private Long idleTimeout;
    private Long holdTimeout;
    private String connectionTestSql;
    private Integer connectionTestTimeout;
    private Long connectionTestIntegererval;
    private Long idleCheckTimeIntegererval;
    private Boolean forceCloseUsingOnClear;
    private Long delayTimeForNextClear;

    private String connectionFactoryClassName;
    private String xaConnectionFactoryClassName;
    private Properties connectProperties;
    private String poolImplementClassName;
    private Boolean enableJmx;

    /**
     * 转换为BeeCP配置
     *
     * @param g 全局配置
     * @return BeeCP配置
     */
    public BeeDataSourceConfig toBeeCpConfig(BeeCpConfig g) {
        BeeDataSourceConfig config = new BeeDataSourceConfig();

        String tempCatalog = defaultCatalog == null ? g.getDefaultCatalog() : defaultCatalog;
        if (tempCatalog != null) {
            config.setDefaultCatalog(tempCatalog);
        }

        String tempSchema = defaultSchema == null ? g.getDefaultSchema() : defaultSchema;
        if (tempSchema != null) {
            config.setDefaultSchema(tempSchema);
        }

        Boolean tempReadOnly = defaultReadOnly == null ? g.getDefaultReadOnly() : defaultReadOnly;
        if (tempReadOnly != null) {
            config.setDefaultReadOnly(tempReadOnly);
        }

        Boolean tempAutoCommit = defaultAutoCommit == null ? g.getDefaultAutoCommit() : defaultAutoCommit;
        if (tempAutoCommit != null) {
            config.setDefaultAutoCommit(tempAutoCommit);
        }

        Integer tempTransactionIsolationCode = defaultTransactionIsolationCode == null ? g.getDefaultTransactionIsolationCode() : defaultTransactionIsolationCode;
        if (tempTransactionIsolationCode != null) {
            config.setDefaultTransactionIsolationCode(tempTransactionIsolationCode);
        }

        String tempTransactionIsolation = defaultTransactionIsolation == null ? g.getDefaultTransactionIsolation() : defaultTransactionIsolation;
        if (tempTransactionIsolation != null) {
            config.setDefaultTransactionIsolation(tempTransactionIsolation);
        }

        Boolean tempFairMode = fairMode == null ? g.getFairMode() : fairMode;
        if (tempFairMode != null) {
            config.setFairMode(tempFairMode);
        }

        Integer tempInitialSize = initialSize == null ? g.getInitialSize() : initialSize;
        if (tempInitialSize != null) {
            config.setInitialSize(tempInitialSize);
        }

        Integer tempMaxActive = maxActive == null ? g.getMaxActive() : maxActive;
        if (tempMaxActive != null) {
            config.setMaxActive(tempMaxActive);
        }

        Integer tempBorrowSemaphoreSize = borrowSemaphoreSize == null ? g.getBorrowSemaphoreSize() : borrowSemaphoreSize;
        if (tempBorrowSemaphoreSize != null) {
            config.setBorrowSemaphoreSize(tempBorrowSemaphoreSize);
        }

        Long tempMaxWait = maxWait == null ? g.getMaxWait() : maxWait;
        if (tempMaxWait != null) {
            config.setMaxWait(tempMaxWait);
        }

        Long tempIdleTimeout = idleTimeout == null ? g.getIdleTimeout() : idleTimeout;
        if (tempIdleTimeout != null) {
            config.setIdleTimeout(tempIdleTimeout);
        }

        Long tempHoldTimeout = holdTimeout == null ? g.getIdleTimeout() : holdTimeout;
        if (tempHoldTimeout != null) {
            config.setHoldTimeout(tempHoldTimeout);
        }

        String tempConnectionTestSql = connectionTestSql == null ? g.getConnectionTestSql() : connectionTestSql;
        if (tempConnectionTestSql != null) {
            config.setConnectionTestSql(tempConnectionTestSql);
        }

        Integer tempConnectionTestTimeout = connectionTestTimeout == null ? g.getConnectionTestTimeout() : connectionTestTimeout;
        if (tempConnectionTestTimeout != null) {
            config.setConnectionTestTimeout(tempConnectionTestTimeout);
        }

        Long tempConnectionTestIntegererval = connectionTestIntegererval == null ? g.getConnectionTestIntegererval() : connectionTestIntegererval;
        if (tempConnectionTestIntegererval != null) {
            config.setConnectionTestInterval(tempConnectionTestIntegererval);
        }

        Long tempIdleCheckTimeIntegererval = idleCheckTimeIntegererval == null ? g.getIdleCheckTimeIntegererval() : idleCheckTimeIntegererval;
        if (tempIdleCheckTimeIntegererval != null) {
            config.setIdleCheckTimeInterval(tempIdleCheckTimeIntegererval);
        }

        Boolean tempForceCloseUsingOnClear = forceCloseUsingOnClear == null ? g.getForceCloseUsingOnClear() : forceCloseUsingOnClear;
        if (tempForceCloseUsingOnClear != null) {
            config.setForceCloseUsingOnClear(tempForceCloseUsingOnClear);
        }

        Long tempDelayTimeForNextClear = delayTimeForNextClear == null ? g.getDelayTimeForNextClear() : delayTimeForNextClear;
        if (tempDelayTimeForNextClear != null) {
            config.setDelayTimeForNextClear(tempDelayTimeForNextClear);
        }

        String tempConnectionFactoryClassName = connectionFactoryClassName == null ? g.getConnectionFactoryClassName() : connectionFactoryClassName;
        if (tempConnectionFactoryClassName != null) {
            config.setConnectionFactoryClassName(tempConnectionFactoryClassName);
        }

        String tempXaConnectionFactoryClassName = xaConnectionFactoryClassName == null ? g.getXaConnectionFactoryClassName() : xaConnectionFactoryClassName;
        if (tempXaConnectionFactoryClassName != null) {
            config.setXaConnectionFactoryClassName(tempXaConnectionFactoryClassName);
        }

        Properties tempConnectProperties = connectProperties == null ? g.getConnectProperties() : connectProperties;
        if (tempConnectProperties != null) {
            config.loadFromProperties(tempConnectProperties);
        }

        String tempPoolImplementClassName = poolImplementClassName == null ? g.getConnectionTestSql() : poolImplementClassName;
        if (tempPoolImplementClassName != null) {
            config.setPoolImplementClassName(tempPoolImplementClassName);
        }

        Boolean tempEnableJmx = enableJmx == null ? g.getEnableJmx() : enableJmx;
        if (tempEnableJmx != null) {
            config.setEnableJmx(tempEnableJmx);
        }

        return config;
    }
}
