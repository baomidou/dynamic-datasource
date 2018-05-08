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

/**
 * <pre>
 *   When you use @DS() and not specify value,We should determine a slave dataSource.
 *   So we can use some Strategy. The default strategy is LoadBalanceDynamicDataSourceStrategy.
 * </pre>
 *
 * @author TaoYu Kanyuxia
 * @see RandomDynamicDataSourceStrategy
 * @see LoadBalanceDynamicDataSourceStrategy
 * @since 1.0.0
 */
@FunctionalInterface
public interface DynamicDataSourceStrategy {

  /**
   * determine a slaveId
   *
   * @param slaveDataSourceLookupKeys slaveKeys
   * @return slaveId
   */
  String determineSlaveDataSource(String[] slaveDataSourceLookupKeys);

}
