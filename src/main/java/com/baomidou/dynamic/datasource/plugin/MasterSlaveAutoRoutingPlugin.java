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

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.toolkit.DdConstants;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * Master-slave Separation Plugin with mybatis
 *
 * @author TaoYu
 * @since 2.5.1
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
        RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})})
@Slf4j
public class MasterSlaveAutoRoutingPlugin implements Interceptor {

  @Autowired
  private DynamicDataSourceProperties properties;

  @Override
  public Object intercept(Invocation invocation) throws Throwable {
    Object[] args = invocation.getArgs();
    MappedStatement ms = (MappedStatement) args[0];
    boolean empty = true;
    try {
      empty = StringUtils.isEmpty(DynamicDataSourceContextHolder.peek());
      if (empty) {
        DynamicDataSourceContextHolder.push(getDataSource(ms));
      }
      return invocation.proceed();
    } finally {
      if (empty) {
        DynamicDataSourceContextHolder.clear();
      }
    }
  }

  /**
   * 获取动态数据源名称，重写注入 DbHealthIndicator 支持数据源健康状况判断选择
   *
   * @param mappedStatement mybatis MappedStatement
   */
  public String getDataSource(MappedStatement mappedStatement) {
    String slave = DdConstants.SLAVE;
    if (properties.isHealth()) {
      /*
       * 根据从库健康状况，判断是否切到主库
       */
      boolean health = DbHealthIndicator.getDbHealth(DdConstants.SLAVE);
      if (!health) {
        health = DbHealthIndicator.getDbHealth(DdConstants.MASTER);
        if (health) {
          slave = DdConstants.MASTER;
        }
      }
    }
    return SqlCommandType.SELECT == mappedStatement.getSqlCommandType() ? slave : DdConstants.MASTER;
  }

  @Override
  public Object plugin(Object target) {
    return target instanceof Executor ? Plugin.wrap(target, this) : target;
  }

  @Override
  public void setProperties(Properties properties) {
  }
}
