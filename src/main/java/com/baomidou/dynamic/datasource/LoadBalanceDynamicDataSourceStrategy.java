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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author TaoYu Kanyuxia
 * @since 1.0.0
 */
public class LoadBalanceDynamicDataSourceStrategy implements DynamicDataSourceStrategy {

  private AtomicInteger count = new AtomicInteger(0);

  @Override
  public String determineSlaveDataSource(String[] slaveDataSourceLookupKeys) {
    int number = count.getAndAdd(1);
    return slaveDataSourceLookupKeys[number % slaveDataSourceLookupKeys.length];
  }

}
