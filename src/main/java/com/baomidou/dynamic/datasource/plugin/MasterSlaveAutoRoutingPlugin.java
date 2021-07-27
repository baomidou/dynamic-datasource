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
package com.baomidou.dynamic.datasource.plugin;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.GroupDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.support.DdConstants;
import com.baomidou.dynamic.datasource.support.HealthCheckAdapter;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Master-slave Separation Plugin with mybatis
 *
 * @author TaoYu
 * @since 2.5.1
 */
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
@Slf4j
public class MasterSlaveAutoRoutingPlugin implements Interceptor {

    @Autowired
    protected DataSource dynamicDataSource;

    @Autowired
    private DynamicDataSourceProperties properties;

    @Lazy
    @Autowired(required = false)
    private HealthCheckAdapter healthCheckAdapter;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        String pushedDataSource = null;
        try {
            String dataSource = getDataSource(ms);
            pushedDataSource = DynamicDataSourceContextHolder.push(dataSource);
            return invocation.proceed();
        } finally {
            if (pushedDataSource != null) {
                DynamicDataSourceContextHolder.poll();
            }
        }
    }

    /**
     * 获取动态数据源名称，重写注入 DbHealthIndicator 支持数据源健康状况判断选择
     *
     * @param mappedStatement mybatis MappedStatement
     * @return 获取真实的数据源名称
     */
    public String getDataSource(MappedStatement mappedStatement) {
        String currentDataSource = SqlCommandType.SELECT == mappedStatement.getSqlCommandType() ? DdConstants.SLAVE : DdConstants.MASTER;
        String dataSource = null;
        if (properties.isHealth()) {
            DynamicRoutingDataSource dynamicRoutingDataSource = (DynamicRoutingDataSource) dynamicDataSource;
            // 当前数据源是从库
            if (DdConstants.SLAVE.equalsIgnoreCase(currentDataSource)) {
                Map<String, GroupDataSource> currentGroupDataSources = dynamicRoutingDataSource.getGroupDataSources();
                GroupDataSource groupDataSource = currentGroupDataSources.get(DdConstants.SLAVE);
                String dsKey = groupDataSource.determineDsKey();
                boolean health = healthCheckAdapter.getHealth(dsKey);
                if (health) {
                    dataSource = dsKey;
                } else {
                    log.warn("从库无法连接, 请检查数据库配置, key: {}", dsKey);
                }
            }
            // 从库无法连接, 或者当前数据源需要操作主库
            if (dataSource == null) {
                // 当前数据源是主库
                Map<String, GroupDataSource> currentGroupDataSources = dynamicRoutingDataSource.getGroupDataSources();
                GroupDataSource groupDataSource = currentGroupDataSources.get(DdConstants.MASTER);
                dataSource = groupDataSource.determineDsKey();
                boolean health = healthCheckAdapter.getHealth(dataSource);
                if (!health) {
                    log.warn("主库无法连接, 请检查数据库配置, key: {}", dataSource);
                }
            }
        } else {
            dataSource = currentDataSource;
        }
        return dataSource;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
