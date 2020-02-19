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
package com.baomidou.dynamic.datasource.creator;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;

/**
 * JNDI数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/27
 */
public class JndiDataSourceCreator {

  private static final JndiDataSourceLookup LOOKUP = new JndiDataSourceLookup();

  /**
   * 创建基础数据源
   *
   * @param name 数据源参数
   * @return 数据源
   */
  public DataSource createDataSource(String name) {
    return LOOKUP.getDataSource(name);
  }

}
