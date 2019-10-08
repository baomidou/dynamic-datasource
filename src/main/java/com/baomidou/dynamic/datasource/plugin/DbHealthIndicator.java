package com.baomidou.dynamic.datasource.plugin;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DdConstants;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库健康状况指标
 *
 * @author hubin
 */
public class DbHealthIndicator extends AbstractHealthIndicator {
    /**
     * 当前执行数据源
     */
    private DataSource dataSource;
    /**
     * 维护数据源健康状况
     */
    private static Map<String, Boolean> DB_HEALTH = new ConcurrentHashMap<>();

    DbHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static String getSlave() {
        String _slave = DdConstants.SLAVE;
        Boolean health = getDbHealth(DdConstants.SLAVE);
        if (null == health || !health) {
            health = getDbHealth(DdConstants.MASTER);
            if (null != health && health) {
                _slave = DdConstants.MASTER;
            }
        }
        return _slave;
    }

    public static String getMaster() {
        String _master = DdConstants.MASTER;
        Boolean health = getDbHealth(DdConstants.MASTER);
        if (null == health || !health) {
            health = getDbHealth(DdConstants.SLAVE);
            if (null != health && health) {
                _master = DdConstants.SLAVE;
            }
        }
        return _master;
    }

    /**
     * 获取数据源连接健康状况
     *
     * @param dataSource 数据源名称
     * @return
     */
    public static boolean getDbHealth(String dataSource) {
        return DB_HEALTH.get(dataSource);
    }

    /**
     * 设置连接池健康状况
     *
     * @param dataSource 数据源名称
     * @param health     健康状况 false 不健康 true 健康
     * @return
     */
    public static Boolean setDbHealth(String dataSource, boolean health) {
        return DB_HEALTH.put(dataSource, health);
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        doDataSourceHealthCheck(builder);
    }

    private void doDataSourceHealthCheck(Health.Builder builder) throws Exception {
        if (dataSource instanceof DynamicRoutingDataSource) {
            Map<String, DataSource> dataSourceMap = ((DynamicRoutingDataSource) dataSource).getCurrentDataSources();
            // 循环检查当前数据源是否可用
            for (Map.Entry<String, DataSource> dataSource : dataSourceMap.entrySet()) {
                Integer result = 0;
                try {
                    result = query(dataSource.getValue());
                } catch (Exception e) {
                    result = 0;
                } finally {
                    DB_HEALTH.put(dataSource.getKey(), 1 == result);
                    builder.withDetail(dataSource.getKey(), result);
                }
            }
        } else {
            Object object = query(dataSource);
            System.out.println(object.getClass());
        }
    }

    private Integer query(DataSource dataSource) throws Exception {
        List<Integer> results = new JdbcTemplate(dataSource).query("SELECT 1", new RowMapper<Integer>() {

            @Override
            public Integer mapRow(ResultSet resultSet, int i) throws SQLException {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columns = metaData.getColumnCount();
                if (columns != 1) {
                    throw new IncorrectResultSetColumnCountException(1, columns);
                }
                return (Integer) JdbcUtils.getResultSetValue(resultSet, 1, Integer.class);
            }
        });
        return DataAccessUtils.requiredSingleResult(results);
    }
}