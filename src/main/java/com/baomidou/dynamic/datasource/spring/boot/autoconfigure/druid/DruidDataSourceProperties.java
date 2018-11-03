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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Properties;

/**
 * Druid常用参数
 *
 * @author TaoYu
 * @since 1.2.0
 */
@Data
@Accessors(chain = true)
public class DruidDataSourceProperties {

    private Integer initialSize;
    private Integer maxActive;
    private Integer minIdle;
    private Long maxWait;
    private Long timeBetweenEvictionRunsMillis;
    private Long minEvictableIdleTimeMillis;
    private Long maxEvictableIdleTimeMillis;
    private String validationQuery;
    private Integer validationQueryTimeout;
    private Boolean testOnBorrow;
    private Boolean testOnReturn;
    private Boolean testWhileIdle;
    private Boolean poolPreparedStatements;
    private Integer maxOpenPreparedStatements;
    private Boolean sharePreparedStatements;
    private Properties connectionProperties;
    private String filters;
}