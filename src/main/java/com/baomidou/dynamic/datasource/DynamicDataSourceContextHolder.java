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
package com.baomidou.dynamic.datasource;

import org.springframework.core.NamedThreadLocal;

/**
 * DynamicDataSourceContextHolder use ThreadLocal to switch dataSource in PerThread.
 *
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
public final class DynamicDataSourceContextHolder {

  private static final ThreadLocal<String> LOOKUP_KEY_HOLDER =  new NamedThreadLocal("current dynamic datasource");

  private DynamicDataSourceContextHolder() {
  }

  public static String getDataSourceLookupKey() {
    return LOOKUP_KEY_HOLDER.get();
  }

  public static void setDataSourceLookupKey(String dataSourceLookupKey) {
    LOOKUP_KEY_HOLDER.set(dataSourceLookupKey);
  }

  public static void clearDataSourceLookupKey() {
    LOOKUP_KEY_HOLDER.remove();
  }

}
