package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.atomikos;

import lombok.Data;

/**
 * Atomikos 配置
 *
 * @author <a href="mailto:312290710@qq.com">jiazhifeng</a>
 * @date 2023/03/02 10:20
 */
@Data
public class AtomikosConfig {
    /**
     * 设置最小池大小。连接的数量不会低于该值。池将在初始化期间打开此数量的连接。可选，默认为 1
     */
    private int minPoolSize = 1;
    /**
     * 设置最大池大小。池连接的数量不会超过这个值。可选，默认为 10。
     */
    private int maxPoolSize = 10;
    /**
     * 设置池在池为空时等待连接在池中可用的最长时间（以秒为单位）。默认为 30
     */
    private int borrowConnectionTimeout = 30;
    /**
     * 设置连接池在声明连接之前允许使用连接的时间量（以秒为单位）。默认值为 0（无超时）
     */
    private int reapTimeout = 0;
    /**
     * 设置未使用的多余连接应保留在池中的最大秒数。选修的。注意：超额连接是在 minPoolSize 限制之上创建的连接。默认值为 60 秒
     */
    private int maxIdleTime = 60;
    /**
     * 设置用于在返回连接之前验证连接的 SQL 查询或语句
     */
    private String testQuery = "SELECT 1";
    /**
     * 设置池维护线程的维护间隔。以秒为单位的时间间隔。如果未设置或不是正数，则将使用池的默认值（60 秒）。
     */
    private int maintenanceInterval = 60;
    /**
     * 设置此数据源返回的连接的默认隔离级别。负值将被忽略并导致特定于供应商的 JDBC 驱动程序或 DBMS 内部默认值。
     */
    private int defaultIsolationLevel = -1;
    /**
     * 设置连接在自动销毁之前保留在池中的最大秒数。可选，默认为 0（无限制）
     */
    private int maxLifetime = 60;
}
