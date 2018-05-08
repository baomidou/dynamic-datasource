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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

/**
 * Default annotation Aspect
 *
 * @author TaoYu Kanyuxia
 * @see DS
 * @since 1.0.0
 */
@Aspect
@Order(-10)
public class DynamicDataSourceAspect {

  @Before("@annotation(ds)")
  public void chooseDataSource(JoinPoint point, DS ds) {
    DynamicDataSourceContextHolder.setDataSourceLookupKey(ds.value());
  }

  @After("@annotation(ds)")
  public void clearDataSource(JoinPoint point, DS ds) {
    DynamicDataSourceContextHolder.clearDataSourceLookupKey();
  }

}