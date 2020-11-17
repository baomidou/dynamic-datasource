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
package com.baomidou.dynamic.datasource.plugin;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.GroupDataSource;
import com.baomidou.dynamic.datasource.exception.CannotSelectDataSourceException;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.support.DbHealthIndicator;
import com.baomidou.dynamic.datasource.support.DdConstants;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
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
    private DynamicDataSourceProperties properties;

    @Lazy
    @Autowired(required = false)
    private DbHealthIndicator dbHealthIndicator;

    @Autowired
    protected DataSource dynamicDataSource;


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
                Map<String, GroupDataSource> currentGroupDataSources = dynamicRoutingDataSource.getCurrentGroupDataSources();
                GroupDataSource groupDataSource = currentGroupDataSources.get(DdConstants.SLAVE);
                String dsKey = groupDataSource.determineDsKey();
                boolean health = dbHealthIndicator.getDbHealth(dsKey);
                if (health) {
                    dataSource = dsKey;
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn("从库无法连接, 请检查数据库配置");
                    }
                }
            }
            // 从库无法连接, 或者当前数据源需要操作主库
            if (dataSource == null) {
                // 当前数据源是从库，并且从库是健康的
                Map<String, GroupDataSource> currentGroupDataSources = dynamicRoutingDataSource.getCurrentGroupDataSources();
                GroupDataSource groupDataSource = currentGroupDataSources.get(DdConstants.MASTER);
                String dsKey = groupDataSource.determineDsKey();
                boolean health = dbHealthIndicator.getDbHealth(dsKey);
                if (health) {
                    dataSource = dsKey;
                } else {
                    if (log.isWarnEnabled()) {
                        log.warn("主库无法连接, 请检查数据库配置");
                    }
                }
            }
            // 主从都连不上, 不应该继续连接数据库了
            if (dataSource == null) {
                throw new CannotSelectDataSourceException("无法选择数据源");
            }
        } else {
            dataSource = currentDataSource;
        }
        return dataSource;
    }

    protected ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    protected ObjectFactory objectFactory = new DefaultObjectFactory();
    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    @Override
    public Object plugin(Object target) {

        if (target instanceof Executor) {
            Object wrapper = target;

            MetaObject executorMetaObject = newMetaObject(target);
            // 支持mybatis缓存和 mp的缓存
            boolean delegate = executorMetaObject.hasGetter("delegate");
            if (delegate) {
                wrapper = executorMetaObject.getValue("delegate");
            }
            Executor baseWrapper = (Executor) wrapper;

            // set base executor to this
            Executor wrapExecutor = (Executor) Plugin.wrap(baseWrapper, this);
            baseWrapper.setExecutorWrapper(wrapExecutor);
            // 将当前对象的代理设置为缓存
            if (delegate) {
                wrapExecutor.setExecutorWrapper((Executor) target);
                executorMetaObject.setValue("delegate", wrapExecutor);
            }
            return wrapExecutor;
        }
        return target;
    }

    private MetaObject newMetaObject(Object target) {
        return MetaObject.forObject(target, objectFactory, objectWrapperFactory, reflectorFactory);
    }

    @Override
    public void setProperties(Properties properties) {
        String orDefault = properties.getProperty("cacheEnable", "true");
    }
}
