/*
 * Copyright © 2018 organization baomidou
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
package com.baomidou.dynamic.datasource.unwrapper;

import org.springframework.core.Ordered;

import javax.sql.DataSource;

/**
 * Unwraps one layer of a datasource proxy without opening a connection.
 * <p>
 * Register implementations in {@code META-INF/services/} using this interface's
 * fully qualified name. Lower order values run first. Implementations must be
 * safe to load when their optional integration is absent. Providers must have
 * a public no-argument constructor and be visible to the thread context
 * classloader used when the chain is first created (or Spring's defining
 * classloader if no context classloader is set). Instances are reused by a
 * routing datasource and must support concurrent calls without retaining
 * per-datasource state.
 *
 * @author Pika-Yalei
 * @since 4.6.0
 */
public interface DataSourceUnwrapper extends Ordered {

    boolean support(DataSource dataSource);

    /**
     * @return the wrapped datasource, or the input (or {@code null}) if unchanged
     * @throws Exception if the supported proxy cannot be unwrapped
     */
    DataSource unwrap(DataSource dataSource) throws Exception;

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
