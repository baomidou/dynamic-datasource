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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.alibaba.druid.pool.DruidAbstractDataSource.*;
import static com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConsts.*;

/**
 * Druid参数配置
 *
 * @author TaoYu
 * @since 1.2.0
 */
@Data
@Slf4j
public class DruidConfig {

    private Integer initialSize;
    private Integer maxActive;
    private Integer minIdle;
    private Integer maxWait;
    private Long timeBetweenEvictionRunsMillis;
    private Long timeBetweenLogStatsMillis;
    private Integer statSqlMaxSize;
    private Long minEvictableIdleTimeMillis;
    private Long maxEvictableIdleTimeMillis;
    private String defaultCatalog;
    private Boolean defaultAutoCommit;
    private Boolean defaultReadOnly;
    private Integer defaultTransactionIsolation;
    private Boolean testWhileIdle;
    private Boolean testOnBorrow;
    private Boolean testOnReturn;
    private String validationQuery;
    private Integer validationQueryTimeout;
    private Boolean useGlobalDataSourceStat;
    private Boolean asyncInit;
    private String filters;
    private Boolean clearFiltersEnable;
    private Boolean resetStatEnable;
    private Integer notFullTimeoutRetryCount;
    private Integer maxWaitThreadCount;
    private Boolean failFast;
    private Long phyTimeoutMillis;
    private Boolean keepAlive;
    private Boolean poolPreparedStatements;
    private Boolean initVariants;
    private Boolean initGlobalVariants;
    private Boolean useUnfairLock;
    private Boolean killWhenSocketReadTimeout;
    private Properties connectionProperties;
    private Integer maxPoolPreparedStatementPerConnectionSize;
    private String initConnectionSqls;
    private Boolean sharePreparedStatements;
    private Integer connectionErrorRetryAttempts;
    private Boolean breakAfterAcquireFailure;
    private Boolean removeAbandoned;
    private Integer removeAbandonedTimeoutMillis;
    private Boolean logAbandoned;
    private Integer queryTimeout;
    private Integer transactionQueryTimeout;
    private String publicKey;

    private Map<String, Object> wall = new HashMap<>();
    private Map<String, Object> slf4j = new HashMap<>();
    private Map<String, Object> log4j = new HashMap<>();
    private Map<String, Object> log4j2 = new HashMap<>();
    private Map<String, Object> commonsLog = new HashMap<>();
    private Map<String, Object> stat = new HashMap<>();

    private List<String> proxyFilters = new ArrayList<>();

    /**
     * 根据全局配置和本地配置结合转换为Properties
     *
     * @param g 全局配置
     * @return Druid配置
     */
    public Properties toProperties(DruidConfig g) {
        Properties properties = new Properties();
        Integer initialSize = this.initialSize == null ? g.getInitialSize() : this.initialSize;
        if (initialSize != null && !initialSize.equals(DEFAULT_INITIAL_SIZE)) {
            properties.setProperty(INITIAL_SIZE, String.valueOf(initialSize));
        }

        Integer maxActive = this.maxActive == null ? g.getMaxActive() : this.maxActive;
        if (maxActive != null && !maxActive.equals(DEFAULT_MAX_WAIT)) {
            properties.setProperty(MAX_ACTIVE, String.valueOf(maxActive));
        }

        Integer minIdle = this.minIdle == null ? g.getMinIdle() : this.minIdle;
        if (minIdle != null && !minIdle.equals(DEFAULT_MIN_IDLE)) {
            properties.setProperty(MIN_IDLE, String.valueOf(minIdle));
        }

        Integer maxWait = this.maxWait == null ? g.getMaxWait() : this.maxWait;
        if (maxWait != null && !maxWait.equals(DEFAULT_MAX_WAIT)) {
            properties.setProperty(MAX_WAIT, String.valueOf(maxWait));
        }

        Long timeBetweenEvictionRunsMillis =
                this.timeBetweenEvictionRunsMillis == null ? g.getTimeBetweenEvictionRunsMillis() : this.timeBetweenEvictionRunsMillis;
        if (timeBetweenEvictionRunsMillis != null && !timeBetweenEvictionRunsMillis.equals(DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS)) {
            properties.setProperty(TIME_BETWEEN_EVICTION_RUNS_MILLIS, String.valueOf(timeBetweenEvictionRunsMillis));
        }

        Long timeBetweenLogStatsMillis =
                this.timeBetweenLogStatsMillis == null ? g.getTimeBetweenLogStatsMillis() : this.timeBetweenLogStatsMillis;
        if (timeBetweenLogStatsMillis != null && timeBetweenLogStatsMillis > 0) {
            properties.setProperty(TIME_BETWEEN_LOG_STATS_MILLIS, String.valueOf(timeBetweenLogStatsMillis));
        }

        Long minEvictableIdleTimeMillis =
                this.minEvictableIdleTimeMillis == null ? g.getMinEvictableIdleTimeMillis() : this.minEvictableIdleTimeMillis;
        if (minEvictableIdleTimeMillis != null && !minEvictableIdleTimeMillis.equals(DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS)) {
            properties.setProperty(MIN_EVICTABLE_IDLE_TIME_MILLIS, String.valueOf(minEvictableIdleTimeMillis));
        }

        Long maxEvictableIdleTimeMillis =
                this.maxEvictableIdleTimeMillis == null ? g.getMaxEvictableIdleTimeMillis() : this.maxEvictableIdleTimeMillis;
        if (maxEvictableIdleTimeMillis != null && !maxEvictableIdleTimeMillis.equals(DEFAULT_MAX_EVICTABLE_IDLE_TIME_MILLIS)) {
            properties.setProperty(MAX_EVICTABLE_IDLE_TIME_MILLIS, String.valueOf(maxEvictableIdleTimeMillis));
        }

        Boolean testWhileIdle = this.testWhileIdle == null ? g.getTestWhileIdle() : this.testWhileIdle;
        if (testWhileIdle != null && !testWhileIdle.equals(DEFAULT_WHILE_IDLE)) {
            properties.setProperty(TEST_WHILE_IDLE, Boolean.FALSE.toString());
        }

        Boolean testOnBorrow = this.testOnBorrow == null ? g.getTestOnBorrow() : this.testOnBorrow;
        if (testOnBorrow != null && !testOnBorrow.equals(DEFAULT_TEST_ON_BORROW)) {
            properties.setProperty(TEST_ON_BORROW, Boolean.TRUE.toString());
        }

        String validationQuery = this.validationQuery == null ? g.getValidationQuery() : this.validationQuery;
        if (validationQuery != null && validationQuery.length() > 0) {
            properties.setProperty(VALIDATION_QUERY, validationQuery);
        }

        Boolean useGlobalDataSourceStat = this.useGlobalDataSourceStat == null ? g.getUseGlobalDataSourceStat() : this.useGlobalDataSourceStat;
        if (useGlobalDataSourceStat != null && useGlobalDataSourceStat.equals(Boolean.TRUE)) {
            properties.setProperty(USE_GLOBAL_DATA_SOURCE_STAT, Boolean.TRUE.toString());
        }

        Boolean asyncInit = this.asyncInit == null ? g.getAsyncInit() : this.asyncInit;
        if (asyncInit != null && asyncInit.equals(Boolean.TRUE)) {
            properties.setProperty(ASYNC_INIT, Boolean.TRUE.toString());
        }

        //filters单独处理，默认了stat
        String filters = this.filters == null ? g.getFilters() : this.filters;
        if (filters == null) {
            filters = STAT_STR;
        }
        if (publicKey != null && publicKey.length() > 0 && !filters.contains(CONFIG_STR)) {
            filters += "," + CONFIG_STR;
        }
        properties.setProperty(FILTERS, filters);

        Boolean clearFiltersEnable = this.clearFiltersEnable == null ? g.getClearFiltersEnable() : this.clearFiltersEnable;
        if (clearFiltersEnable != null && clearFiltersEnable.equals(Boolean.FALSE)) {
            properties.setProperty(CLEAR_FILTERS_ENABLE, Boolean.FALSE.toString());
        }

        Boolean resetStatEnable = this.resetStatEnable == null ? g.getResetStatEnable() : this.resetStatEnable;
        if (resetStatEnable != null && resetStatEnable.equals(Boolean.FALSE)) {
            properties.setProperty(RESET_STAT_ENABLE, Boolean.FALSE.toString());
        }

        Integer notFullTimeoutRetryCount =
                this.notFullTimeoutRetryCount == null ? g.getNotFullTimeoutRetryCount() : this.notFullTimeoutRetryCount;
        if (notFullTimeoutRetryCount != null && !notFullTimeoutRetryCount.equals(0)) {
            properties.setProperty(NOT_FULL_TIMEOUT_RETRY_COUNT, String.valueOf(notFullTimeoutRetryCount));
        }

        Integer maxWaitThreadCount = this.maxWaitThreadCount == null ? g.getMaxWaitThreadCount() : this.maxWaitThreadCount;
        if (maxWaitThreadCount != null && !maxWaitThreadCount.equals(-1)) {
            properties.setProperty(MAX_WAIT_THREAD_COUNT, String.valueOf(maxWaitThreadCount));
        }

        Boolean failFast = this.failFast == null ? g.getFailFast() : this.failFast;
        if (failFast != null && failFast.equals(Boolean.TRUE)) {
            properties.setProperty(FAIL_FAST, Boolean.TRUE.toString());
        }

        Long phyTimeoutMillis = this.phyTimeoutMillis == null ? g.getPhyTimeoutMillis() : this.phyTimeoutMillis;
        if (phyTimeoutMillis != null && !phyTimeoutMillis.equals(DEFAULT_PHY_TIMEOUT_MILLIS)) {
            properties.setProperty(PHY_TIMEOUT_MILLIS, String.valueOf(phyTimeoutMillis));
        }

        Boolean keepAlive = this.keepAlive == null ? g.getKeepAlive() : this.keepAlive;
        if (keepAlive != null && keepAlive.equals(Boolean.TRUE)) {
            properties.setProperty(KEEP_ALIVE, Boolean.TRUE.toString());
        }

        Boolean poolPreparedStatements = this.poolPreparedStatements == null ? g.getPoolPreparedStatements() : this.poolPreparedStatements;
        if (poolPreparedStatements != null && poolPreparedStatements.equals(Boolean.TRUE)) {
            properties.setProperty(POOL_PREPARED_STATEMENTS, Boolean.TRUE.toString());
        }

        Boolean initVariants = this.initVariants == null ? g.getInitVariants() : this.initVariants;
        if (initVariants != null && initVariants.equals(Boolean.TRUE)) {
            properties.setProperty(INIT_VARIANTS, Boolean.TRUE.toString());
        }

        Boolean initGlobalVariants = this.initGlobalVariants == null ? g.getInitGlobalVariants() : this.initGlobalVariants;
        if (initGlobalVariants != null && initGlobalVariants.equals(Boolean.TRUE)) {
            properties.setProperty(INIT_GLOBAL_VARIANTS, Boolean.TRUE.toString());
        }

        Boolean useUnfairLock = this.useUnfairLock == null ? g.getUseUnfairLock() : this.useUnfairLock;
        if (useUnfairLock != null) {
            properties.setProperty(USE_UNFAIR_LOCK, String.valueOf(useUnfairLock));
        }

        Boolean killWhenSocketReadTimeout =
                this.killWhenSocketReadTimeout == null ? g.getKillWhenSocketReadTimeout() : this.killWhenSocketReadTimeout;
        if (killWhenSocketReadTimeout != null && killWhenSocketReadTimeout.equals(Boolean.TRUE)) {
            properties.setProperty(KILL_WHEN_SOCKET_READ_TIMEOUT, Boolean.TRUE.toString());
        }

        Properties connectProperties = connectionProperties == null ? g.getConnectionProperties() : connectionProperties;

        if (publicKey != null && publicKey.length() > 0) {
            if (connectProperties == null) {
                connectProperties = new Properties();
            }
            connectProperties.setProperty("config.decrypt", Boolean.TRUE.toString());
            connectProperties.setProperty("config.decrypt.key", publicKey);
        }
        this.connectionProperties = connectProperties;

        Integer maxPoolPreparedStatementPerConnectionSize =
                this.maxPoolPreparedStatementPerConnectionSize == null ? g.getMaxPoolPreparedStatementPerConnectionSize()
                        : this.maxPoolPreparedStatementPerConnectionSize;
        if (maxPoolPreparedStatementPerConnectionSize != null && !maxPoolPreparedStatementPerConnectionSize.equals(10)) {
            properties.setProperty(MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE, String.valueOf(maxPoolPreparedStatementPerConnectionSize));
        }

        String initConnectionSqls = this.initConnectionSqls == null ? g.getInitConnectionSqls() : this.initConnectionSqls;
        if (initConnectionSqls != null && initConnectionSqls.length() > 0) {
            properties.setProperty(INIT_CONNECTION_SQLS, initConnectionSqls);
        }
        return properties;
    }

    public List<String> getProxyFilters() {
        return proxyFilters;
    }

    public void setProxyFilters(List<String> proxyFilters) {
        this.proxyFilters = proxyFilters;
    }
}