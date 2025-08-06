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
package com.baomidou.dynamic.datasource.creator.druid;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Druid参数配置
 *
 * @author TaoYu
 * @since 1.2.0
 */
@Getter
@Setter
public class DruidConfig {

    private Integer initialSize;
    private Integer maxActive;
    private Integer minIdle;
    private Integer maxWait;
    private Long timeBetweenEvictionRunsMillis;
    private Long timeBetweenLogStatsMillis;
    private Long keepAliveBetweenTimeMillis;
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
    private Long phyMaxUseCount;

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
    private Integer connectTimeout;
    private Integer socketTimeout;
    private Long timeBetweenConnectErrorMillis;
    private Integer maxCreateTaskCount;
    private Integer createSchedulerCorePoolSize;
    private Integer destroySchedulerCorePoolSize;

    private Map<String, Object> wall = new HashMap<>();
    private Map<String, Object> slf4j = new HashMap<>();
    private Map<String, Object> log4j = new HashMap<>();
    private Map<String, Object> log4j2 = new HashMap<>();
    private Map<String, Object> commonsLog = new HashMap<>();
    private Map<String, Object> stat = new HashMap<>();

    private String proxyFilters;
}