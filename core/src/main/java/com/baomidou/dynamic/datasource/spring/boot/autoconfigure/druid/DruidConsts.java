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

/**
 * Druid 配置属性
 *
 * @author TaoYu
 * @since 2020/1/27
 */
public interface DruidConsts {

    String INITIAL_SIZE = "initialSize";
    String MAX_ACTIVE = "maxActive";
    String MIN_IDLE = "minIdle";
    String MAX_WAIT = "maxWait";

    String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "timeBetweenEvictionRunsMillis";
    String TIME_BETWEEN_LOG_STATS_MILLIS = "timeBetweenLogStatsMillis";
    String MIN_EVICTABLE_IDLE_TIME_MILLIS = "minEvictableIdleTimeMillis";
    String MAX_EVICTABLE_IDLE_TIME_MILLIS = "maxEvictableIdleTimeMillis";

    String TEST_WHILE_IDLE = "testWhileIdle";
    String TEST_ON_BORROW = "testOnBorrow";
    String VALIDATION_QUERY = "validationQuery";
    String USE_GLOBAL_DATA_SOURCE_STAT = "useGlobalDataSourceStat";
    String ASYNC_INIT = "asyncInit";

    String FILTERS = "filters";
    String CLEAR_FILTERS_ENABLE = "clearFiltersEnable";
    String RESET_STAT_ENABLE = "resetStatEnable";
    String NOT_FULL_TIMEOUT_RETRY_COUNT = "notFullTimeoutRetryCount";
    String MAX_WAIT_THREAD_COUNT = "maxWaitThreadCount";

    String FAIL_FAST = "failFast";
    String PHY_TIMEOUT_MILLIS = "phyTimeoutMillis";
    String KEEP_ALIVE = "keepAlive";
    String POOL_PREPARED_STATEMENTS = "poolPreparedStatements";
    String INIT_VARIANTS = "initVariants";
    String INIT_GLOBAL_VARIANTS = "initGlobalVariants";
    String USE_UNFAIR_LOCK = "useUnfairLock";
    String KILL_WHEN_SOCKET_READ_TIMEOUT = "killWhenSocketReadTimeout";
    String MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE = "maxPoolPreparedStatementPerConnectionSize";
    String INIT_CONNECTION_SQLS = "initConnectionSqls";

    String CONFIG_STR = "config";
    String STAT_STR = "stat";
}
