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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.dbcp;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import static com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConsts.TEST_WHILE_IDLE;


@Data
@Accessors(chain = true)
@NoArgsConstructor
@Slf4j
/**
 * jdbc 相关配置
 * @author liushang
 * @since 2020/1/27
 */
public class DbcpConfig {
    private String url;
    private String driver;
    private String username;
    private String password;
    private Integer initialSize;
    private Integer maxIdle;
    private Integer minIdle;
    private Integer maxActive;
    private Boolean logAbandoned;
    private Boolean removeAbandoned;
    private Integer removeAbandonedTimeout;
    private Integer maxWait;

    private Boolean testOnBorrow;
    private Boolean testOnReturn;
    private Boolean testWhileIdle;
    private String validationQuery;
    private Integer validationQueryTimeout;
    private Integer timeBetweenEvictionRunsMillis;
    private Integer numTestsPerEvictionRun;

}
