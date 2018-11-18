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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import static com.alibaba.druid.pool.DruidAbstractDataSource.*;

/**
 * Druid常用参数
 *
 * @author TaoYu
 * @since 1.2.0
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@Slf4j
public class DruidConfig {

    private Integer initialSize;
    private Integer maxActive;
    private Integer minIdle;
    private Long maxWait;
    private Long timeBetweenEvictionRunsMillis;
    private Long timeBetweenLogStatsMillis;
    private Integer statSqlMaxSize;
    private Long minEvictableIdleTimeMillis;
    private Long maxEvictableIdleTimeMillis;
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
    private Long maxWaitThreadCount;
    private Boolean failFast;
    private Integer phyTimeoutMillis;
    private Boolean keepAlive;
    private Boolean poolPreparedStatements;
    private Boolean initVariants;
    private Boolean initGlobalVariants;
    private Boolean useUnfairLock;
    private Boolean killWhenSocketReadTimeout;
    private String connectionProperties;
    private Integer maxPoolPreparedStatementPerConnectionSize;
    private String initConnectionSqls;
    private Boolean sharePreparedStatements;
    private Integer connectionErrorRetryAttempts;
    private Boolean breakAfterAcquireFailure;

    private String publicKey;

    public Properties toProperties(DruidConfig config) {
        Properties properties = new Properties();
        Integer initialSize = this.initialSize == null ? config.getInitialSize() : this.initialSize;
        if (initialSize != null && !initialSize.equals(DEFAULT_INITIAL_SIZE)) {
            properties.setProperty("druid.initialSize", String.valueOf(initialSize));
        }

        Integer maxActive = this.maxActive == null ? config.getMaxActive() : this.maxActive;
        if (maxActive != null && !maxActive.equals(DEFAULT_MAX_WAIT)) {
            properties.setProperty("druid.maxActive", String.valueOf(maxActive));
        }

        Integer minIdle = this.minIdle == null ? config.getMinIdle() : this.minIdle;
        if (minIdle != null && !minIdle.equals(DEFAULT_MIN_IDLE)) {
            properties.setProperty("druid.minIdle", String.valueOf(minIdle));
        }

        Long maxWait = this.maxWait == null ? config.getMaxWait() : this.maxWait;
        if (maxWait != null && !maxWait.equals(DEFAULT_MAX_WAIT)) {
            properties.setProperty("druid.maxWait", String.valueOf(maxWait));
        }

        Long timeBetweenEvictionRunsMillis = this.timeBetweenEvictionRunsMillis == null ? config.getTimeBetweenEvictionRunsMillis() : this.timeBetweenEvictionRunsMillis;
        if (timeBetweenEvictionRunsMillis != null && !timeBetweenEvictionRunsMillis.equals(DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS)) {
            properties.setProperty("druid.timeBetweenEvictionRunsMillis", String.valueOf(timeBetweenEvictionRunsMillis));
        }

        Long timeBetweenLogStatsMillis = this.timeBetweenLogStatsMillis == null ? config.getTimeBetweenLogStatsMillis() : this.timeBetweenLogStatsMillis;
        if (timeBetweenLogStatsMillis != null && timeBetweenLogStatsMillis > 0) {
            properties.setProperty("druid.timeBetweenLogStatsMillis", String.valueOf(timeBetweenLogStatsMillis));
        }

        Integer statSqlMaxSize = this.statSqlMaxSize == null ? config.getStatSqlMaxSize() : this.statSqlMaxSize;
        if (statSqlMaxSize != null) {
            properties.setProperty("druid.stat.sql.MaxSize", String.valueOf(statSqlMaxSize));
        }

        Long minEvictableIdleTimeMillis = this.minEvictableIdleTimeMillis == null ? config.getMinEvictableIdleTimeMillis() : this.minEvictableIdleTimeMillis;
        if (minEvictableIdleTimeMillis != null && !minEvictableIdleTimeMillis.equals(DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS)) {
            properties.setProperty("druid.minEvictableIdleTimeMillis", String.valueOf(minEvictableIdleTimeMillis));
        }

        Long maxEvictableIdleTimeMillis = this.maxEvictableIdleTimeMillis == null ? config.getMaxEvictableIdleTimeMillis() : this.maxEvictableIdleTimeMillis;
        if (maxEvictableIdleTimeMillis != null && !maxEvictableIdleTimeMillis.equals(DEFAULT_MAX_EVICTABLE_IDLE_TIME_MILLIS)) {
            properties.setProperty("druid.maxEvictableIdleTimeMillis", String.valueOf(maxEvictableIdleTimeMillis));
        }

        Boolean testWhileIdle = this.testWhileIdle == null ? config.getTestWhileIdle() : this.testWhileIdle;
        if (testWhileIdle != null && !testWhileIdle.equals(DEFAULT_WHILE_IDLE)) {
            properties.setProperty("druid.testWhileIdle", "false");
        }

        Boolean testOnBorrow = this.testOnBorrow == null ? config.getTestOnBorrow() : this.testOnBorrow;
        if (testOnBorrow != null && !testOnBorrow.equals(DEFAULT_TEST_ON_BORROW)) {
            properties.setProperty("druid.testOnBorrow", "true");
        }

        String validationQuery = this.validationQuery == null ? config.getValidationQuery() : this.validationQuery;
        if (validationQuery != null && validationQuery.length() > 0) {
            properties.setProperty("druid.validationQuery", validationQuery);
        }

        Boolean useGlobalDataSourceStat = this.useGlobalDataSourceStat == null ? config.getUseGlobalDataSourceStat() : this.useGlobalDataSourceStat;
        if (useGlobalDataSourceStat != null && useGlobalDataSourceStat.equals(Boolean.TRUE)) {
            properties.setProperty("druid.useGlobalDataSourceStat", "true");
        }

        Boolean asyncInit = this.asyncInit == null ? config.getAsyncInit() : this.asyncInit;
        if (asyncInit != null && asyncInit.equals(Boolean.TRUE)) {
            properties.setProperty("druid.asyncInit", "true");
        }

        //filters单独处理，默认了stat,wall
        String filters = this.filters == null ? config.getFilters() : this.filters;
        if (filters == null) {
            filters = "stat,wall";
        }
        if (publicKey != null && publicKey.length() > 0 && !filters.contains("config")) {
            filters += ",config";
        }
        properties.setProperty("druid.filters", filters);

        Boolean clearFiltersEnable = this.clearFiltersEnable == null ? config.getClearFiltersEnable() : this.clearFiltersEnable;
        if (clearFiltersEnable != null && clearFiltersEnable.equals(Boolean.FALSE)) {
            properties.setProperty("druid.clearFiltersEnable", "false");
        }

        Boolean resetStatEnable = this.resetStatEnable == null ? config.getResetStatEnable() : this.resetStatEnable;
        if (resetStatEnable != null && resetStatEnable.equals(Boolean.FALSE)) {
            properties.setProperty("druid.resetStatEnable", "false");
        }

        Integer notFullTimeoutRetryCount = this.notFullTimeoutRetryCount == null ? config.getNotFullTimeoutRetryCount() : this.notFullTimeoutRetryCount;
        if (notFullTimeoutRetryCount != null && !notFullTimeoutRetryCount.equals(0)) {
            properties.setProperty("druid.notFullTimeoutRetryCount", String.valueOf(notFullTimeoutRetryCount));
        }

        Long maxWaitThreadCount = this.maxWaitThreadCount == null ? config.getMaxWaitThreadCount() : this.maxWaitThreadCount;
        if (maxWaitThreadCount != null && !maxWaitThreadCount.equals(DEFAULT_MAX_WAIT)) {
            properties.setProperty("druid.maxWaitThreadCount", String.valueOf(maxWaitThreadCount));
        }

        Boolean failFast = this.failFast == null ? config.getFailFast() : this.failFast;
        if (failFast != null && failFast.equals(Boolean.TRUE)) {
            properties.setProperty("druid.failFast", "true");
        }

        Integer phyTimeoutMillis = this.phyTimeoutMillis == null ? config.getPhyTimeoutMillis() : this.phyTimeoutMillis;
        if (phyTimeoutMillis != null && !phyTimeoutMillis.equals(DEFAULT_PHY_TIMEOUT_MILLIS)) {
            properties.setProperty("druid.phyTimeoutMillis", String.valueOf(phyTimeoutMillis));
        }

        Boolean keepAlive = this.keepAlive == null ? config.getKeepAlive() : this.keepAlive;
        if (keepAlive != null && keepAlive.equals(Boolean.TRUE)) {
            properties.setProperty("druid.keepAlive", "true");
        }

        Boolean poolPreparedStatements = this.poolPreparedStatements == null ? config.getPoolPreparedStatements() : this.poolPreparedStatements;
        if (poolPreparedStatements != null && poolPreparedStatements.equals(Boolean.TRUE)) {
            properties.setProperty("druid.poolPreparedStatements", "true");
        }

        Boolean initVariants = this.initVariants == null ? config.getInitVariants() : this.initVariants;
        if (initVariants != null && initVariants.equals(Boolean.TRUE)) {
            properties.setProperty("druid.initVariants", "true");
        }

        Boolean initGlobalVariants = this.initGlobalVariants == null ? config.getInitGlobalVariants() : this.initGlobalVariants;
        if (initGlobalVariants != null && initGlobalVariants.equals(Boolean.TRUE)) {
            properties.setProperty("druid.initGlobalVariants", "true");
        }

        Boolean useUnfairLock = this.useUnfairLock == null ? config.getUseUnfairLock() : this.useUnfairLock;
        if (useUnfairLock != null) {
            properties.setProperty("druid.useUnfairLock", String.valueOf(useUnfairLock));
        }

        Boolean killWhenSocketReadTimeout = this.killWhenSocketReadTimeout == null ? config.getKillWhenSocketReadTimeout() : this.killWhenSocketReadTimeout;
        if (killWhenSocketReadTimeout != null && killWhenSocketReadTimeout.equals(Boolean.TRUE)) {
            properties.setProperty("druid.killWhenSocketReadTimeout", "true");
        }

        String connectProperties = this.connectionProperties == null ? config.getConnectionProperties() : this.connectionProperties;

        if (this.publicKey != null && this.publicKey.length() > 0) {
            if (connectProperties == null) {
                connectProperties = "";
            }
            log.info("动态数据源-检测到您配置了druid加密,加密所需连接参数已为您自动配置");
            connectProperties += "config.decrypt=true;config.decrypt.key=" + this.publicKey;
        }
        if (connectProperties != null && connectProperties.length() > 0) {
            properties.setProperty("druid.connectProperties", connectProperties);
        }

        Integer maxPoolPreparedStatementPerConnectionSize = this.maxPoolPreparedStatementPerConnectionSize == null ? config.getMaxPoolPreparedStatementPerConnectionSize() : this.maxPoolPreparedStatementPerConnectionSize;
        if (maxPoolPreparedStatementPerConnectionSize != null && !maxPoolPreparedStatementPerConnectionSize.equals(10)) {
            properties.setProperty("druid.maxPoolPreparedStatementPerConnectionSize", String.valueOf(maxPoolPreparedStatementPerConnectionSize));
        }

        String initConnectionSqls = this.initConnectionSqls == null ? config.getInitConnectionSqls() : this.initConnectionSqls;
        if (initConnectionSqls != null && initConnectionSqls.length() > 0) {
            properties.setProperty("druid.initConnectionSqls", initConnectionSqls);
        }
        return properties;
    }
}