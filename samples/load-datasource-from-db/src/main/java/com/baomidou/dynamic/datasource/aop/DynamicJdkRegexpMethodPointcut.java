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

import java.util.Map;
import org.springframework.aop.support.JdkRegexpMethodPointcut;

/**
 * @author TaoYu
 * @since 2.5.0
 */
public class DynamicJdkRegexpMethodPointcut extends JdkRegexpMethodPointcut {

  private Map<String, String> matchesCache;

  private String ds;

  public DynamicJdkRegexpMethodPointcut(String pattern, String ds, Map<String, String> matchesCache) {
    this.ds = ds;
    this.matchesCache = matchesCache;
    setPattern(pattern);
  }

  @Override
  protected boolean matches(String pattern, int patternIndex) {
    boolean matches = super.matches(pattern, patternIndex);
    if (matches) {
      matchesCache.put(pattern, ds);
    }
    return matches;
  }
}