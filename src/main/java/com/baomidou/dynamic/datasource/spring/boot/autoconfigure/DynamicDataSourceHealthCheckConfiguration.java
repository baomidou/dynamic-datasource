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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.baomidou.dynamic.datasource.support.DbHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * @author liushang@zsyjr.com
 */
@ConditionalOnClass(AbstractHealthIndicator.class)
@ConditionalOnEnabledHealthIndicator("dynamicDS")
@Configuration
public class DynamicDataSourceHealthCheckConfiguration {

    private static final String DYNAMIC_HEALTH_CHECK = DynamicDataSourceProperties.PREFIX + ".health";

    @Bean("dynamicDataSourceHealthCheck")
    @ConditionalOnProperty(DYNAMIC_HEALTH_CHECK)
    public DbHealthIndicator healthIndicator(DataSource dataSource, DynamicDataSourceProperties dynamicDataSourceProperties) {
        return new DbHealthIndicator(dataSource, dynamicDataSourceProperties.getHealthValidQuery());
    }
}
