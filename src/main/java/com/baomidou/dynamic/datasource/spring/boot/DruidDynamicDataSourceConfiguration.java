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
package com.baomidou.dynamic.datasource.spring.boot;

import com.alibaba.druid.filter.config.ConfigFilter;
import com.alibaba.druid.filter.encoding.EncodingConvertFilter;
import com.alibaba.druid.filter.logging.CommonsLogFilter;
import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidFilterConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidSpringAopConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidStatViewServletConfiguration;
import com.alibaba.druid.spring.boot.autoconfigure.stat.DruidWebStatFilterConfiguration;
import com.alibaba.druid.wall.WallFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnClass(DruidDataSource.class)
@EnableConfigurationProperties({DruidStatProperties.class})
@Import({
    DruidSpringAopConfiguration.class,
    DruidStatViewServletConfiguration.class,
    DruidWebStatFilterConfiguration.class,
    DruidFilterConfiguration.class})
public class DruidDynamicDataSourceConfiguration {

}
