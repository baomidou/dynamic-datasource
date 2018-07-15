/**
 * Copyright © 2018 TaoYu (tracy5546@gmail.com)
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
package com.baomidou.dynamic.datasource.aop;

import com.baomidou.dynamic.datasource.DS;
import com.baomidou.dynamic.datasource.DynamicDataSourceContextHolder;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author TaoYu
 * @since 1.2.0
 */
public class DynamicDatasourceAnnotationInterceptor implements MethodInterceptor {

  private Boolean forceMaster;

  public DynamicDatasourceAnnotationInterceptor(Boolean forceMaster) {
    this.forceMaster = forceMaster;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    try {
      String datasource = determineDatasource(invocation.getMethod());
      DynamicDataSourceContextHolder.setDataSourceLookupKey(datasource);
      return invocation.proceed();
    } finally {
      DynamicDataSourceContextHolder.clearDataSourceLookupKey();
    }
  }

  private String determineDatasource(Method method) {
    if (forceMaster && isTransactional(method)) {
      return "master";
    }
    return method.isAnnotationPresent(DS.class) ? method.getAnnotation(DS.class).value()
        : AnnotationUtils.findAnnotation(method.getDeclaringClass(), DS.class).value();
  }

  //fixme 这里判断是否存在事物的方法不太正确,是否有更好的办法判断当前方法是否存在事物， 值得注意的是数据源切面在事物切面之前
  private Boolean isTransactional(Method method) {
    return method.isAnnotationPresent(Transactional.class) || method.getDeclaringClass().isAnnotationPresent(Transactional.class);
  }

}