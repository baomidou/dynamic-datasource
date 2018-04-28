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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 *   Use DS annotation on service method or mybatis mapper method.
 *   If you not specify value,We use DynamicDataSourceStrategy to determine a slave dataSource
 *   If you specify value like @DS("slave1"),but it is not exist. It will use default masterDataSource.
 * </pre>
 *
 * @author TaoYu Kanyuxia
 * @see DynamicDataSourceStrategy
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DS {

  String value() default "";

}