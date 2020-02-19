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
package com.baomidou.dynamic.datasource.processor;

import javax.servlet.http.HttpServletRequest;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author TaoYu
 * @since 2.5.0
 */
public class DsHeaderProcessor extends DsProcessor {

  /**
   * header prefix
   */
  private static final String HEADER_PREFIX = "#header";

  @Override
  public boolean matches(String key) {
    return key.startsWith(HEADER_PREFIX);
  }

  @Override
  public String doDetermineDatasource(MethodInvocation invocation, String key) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    return request.getHeader(key.substring(8));
  }
}
