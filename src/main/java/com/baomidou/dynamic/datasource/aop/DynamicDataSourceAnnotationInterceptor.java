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
package com.baomidou.dynamic.datasource.aop;

import com.baomidou.dynamic.datasource.DynamicDataSourceClassResolver;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.processor.DsProcessor;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import java.lang.reflect.Method;
import lombok.Setter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Core Interceptor of Dynamic Datasource
 *
 * @author TaoYu
 * @since 1.2.0
 */
public class DynamicDataSourceAnnotationInterceptor implements MethodInterceptor {

  /**
   * The identification of SPEL.
   */
  private static final String DYNAMIC_PREFIX = "#";
  private static final DynamicDataSourceClassResolver RESOLVER = new DynamicDataSourceClassResolver();
  @Setter
  private DsProcessor dsProcessor;

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    try {
      DynamicDataSourceContextHolder.push(determineDatasource(invocation));
      return invocation.proceed();
    } finally {
      DynamicDataSourceContextHolder.poll();
    }
  }

  private String determineDatasource(MethodInvocation invocation) throws Throwable {
    Method method = invocation.getMethod();
    DS ds = method.isAnnotationPresent(DS.class)
        ? method.getAnnotation(DS.class)
        : AnnotationUtils.findAnnotation(RESOLVER.targetClass(invocation), DS.class);
    String key = ds.value();
    return (!key.isEmpty() && key.startsWith(DYNAMIC_PREFIX)) ? dsProcessor
        .determineDatasource(invocation, key) : key;
  }
}