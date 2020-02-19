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
package com.baomidou.dynamic.datasource;

import com.baomidou.dynamic.datasource.matcher.ExpressionMatcher;
import com.baomidou.dynamic.datasource.matcher.Matcher;
import com.baomidou.dynamic.datasource.matcher.RegexMatcher;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;

/**
 * 基于多种策略的自动切换数据源
 *
 * @author TaoYu
 * @since 2.5.0
 */
public class DynamicDataSourceConfigure {

  @Getter
  private List<Matcher> matchers = new LinkedList<>();

  private DynamicDataSourceConfigure() {
  }

  public static DynamicDataSourceConfigure config() {
    return new DynamicDataSourceConfigure();
  }

  public DynamicDataSourceConfigure regexMatchers(String pattern, String ds) {
    matchers.add(new RegexMatcher(pattern, ds));
    return this;
  }

  public DynamicDataSourceConfigure expressionMatchers(String expression, String ds) {
    matchers.add(new ExpressionMatcher(expression, ds));
    return this;
  }

}
