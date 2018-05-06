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

import com.baomidou.dynamic.datasource.DynamicDataSourceContextHolder;
import java.lang.reflect.Method;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * @author TaoYu
 * @since 1.2.0
 */
public abstract class AbstractDynamicDatasourceInterceptor implements MethodBeforeAdvice {

  @Override
  public void before(Method method, Object[] args, Object target) throws Throwable {
    String datasource = determineDatasource(method, args, target);
    DynamicDataSourceContextHolder.setDataSourceLookupKey(datasource);
  }

  /**
   * determine datasourceKey you want to use
   * <pre>
   *   keyName to keyName eg: master  slave1, if not exist to master
   *   "" to auto
   * </pre>
   */
  protected abstract String determineDatasource(Method method, Object[] args, Object target);

}
