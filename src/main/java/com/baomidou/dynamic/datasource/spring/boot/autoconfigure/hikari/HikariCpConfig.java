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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.hikari;

import com.zaxxer.hikari.HikariConfigMXBean;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * HikariCp参数配置
 *
 * @author TaoYu
 * @since 2.4.1
 */
@Data
@Slf4j
public class HikariCpConfig implements HikariConfigMXBean {

    private String catalog;
    private volatile long connectionTimeout;
    private volatile long validationTimeout;
    private volatile long idleTimeout;
    private volatile long leakDetectionThreshold;
    private volatile long maxLifetime;
    private volatile int maxPoolSize;
    private volatile int minIdle;
    private volatile long initializationFailTimeout;
    private String connectionInitSql;
    private String connectionTestQuery;
    private String dataSourceClassName;
    private String dataSourceJndiName;
    private String transactionIsolationName;
    private Boolean isAutoCommit;
    private Boolean isReadOnly;
    private Boolean isIsolateInternalQueries;
    private Boolean isRegisterMbeans;
    private Boolean isAllowPoolSuspension;
    private Properties dataSourceProperties;
    private Properties healthCheckProperties;
    private String password;
    private String poolName;
    private String username;
    /**
     * 高版本才有
     */
    private String schema;
    private String exceptionOverrideClassName;
    private Long keepaliveTime;
    private Boolean sealed;

    @Override
    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public void setValidationTimeout(long validationTimeout) {
        this.validationTimeout = validationTimeout;
    }

    @Override
    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @Override
    public void setLeakDetectionThreshold(long leakDetectionThreshold) {
        this.leakDetectionThreshold = leakDetectionThreshold;
    }

    @Override
    public void setMaxLifetime(long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    @Override
    public int getMinimumIdle() {
        return minIdle;
    }

    @Override
    public void setMinimumIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    @Override
    public int getMaximumPoolSize() {
        return maxPoolSize;
    }

    @Override
    public void setMaximumPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPoolName() {
        return this.poolName;
    }
}
