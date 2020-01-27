package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid;

/**
 * Druid 配置属性
 *
 * @author TaoYu
 * @date 2020/1/27
 */
public interface DruidConsts {

  String INITIAL_SIZE = "druid.initialSize";
  String MAX_ACTIVE = "druid.maxActive";
  String MIN_IDLE = "druid.minIdle";
  String MAX_WAIT = "druid.maxWait";

  String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "druid.timeBetweenEvictionRunsMillis";
  String TIME_BETWEEN_LOG_STATS_MILLIS = "druid.timeBetweenLogStatsMillis";
  String MIN_EVICTABLE_IDLE_TIME_MILLIS = "druid.minEvictableIdleTimeMillis";
  String MAX_EVICTABLE_IDLE_TIME_MILLIS = "druid.maxEvictableIdleTimeMillis";

  String TEST_WHILE_IDLE = "druid.testWhileIdle";
  String TEST_ON_BORROW = "druid.testOnBorrow";
  String VALIDATION_QUERY = "druid.validationQuery";
  String USE_GLOBAL_DATA_SOURCE_STAT = "druid.useGlobalDataSourceStat";
  String ASYNC_INIT = "druid.asyncInit";

  String FILTERS = "druid.filters";
  String CLEAR_FILTERS_ENABLE = "druid.clearFiltersEnable";
  String RESET_STAT_ENABLE = "druid.resetStatEnable";
  String NOT_FULL_TIMEOUT_RETRY_COUNT = "druid.notFullTimeoutRetryCount";
  String MAX_WAIT_THREAD_COUNT = "druid.maxWaitThreadCount";

  String FAIL_FAST = "druid.failFast";
  String PHY_TIMEOUT_MILLIS = "druid.phyTimeoutMillis";
  String KEEP_ALIVE = "druid.keepAlive";
  String POOL_PREPARED_STATEMENTS = "druid.poolPreparedStatements";
  String INIT_VARIANTS = "druid.initVariants";
  String INIT_GLOBAL_VARIANTS = "druid.initGlobalVariants";
  String USE_UNFAIR_LOCK = "druid.useUnfairLock";
  String KILL_WHEN_SOCKET_READ_TIMEOUT = "druid.killWhenSocketReadTimeout";
  String MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE = "druid.maxPoolPreparedStatementPerConnectionSize";
  String INIT_CONNECTION_SQLS = "druid.initConnectionSqls";

  String STAT_SQL_MAX_SIZE = "druid.stat.sql.MaxSize";
  String STAT_LOG_SLOW_SQL = "druid.stat.logSlowSql";
  String STAT_SLOW_SQL_MILLIS = "druid.stat.slowSqlMillis";
  String STAT_MERGE_SQL = "druid.stat.mergeSql";
}
