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

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * Ordered datasource unwrappers, shared by lifecycle and diagnostic consumers.
 *
 * @author Pika-Yalei
 * @since 4.6.0
 */
public class DataSourceUnwrapperChain {

    private final List<DataSourceUnwrapper> unwrappers = new ArrayList<>();

    /**
     * Retains the routing datasource's opt-in flags for the built-in integrations.
     * Service providers are independent of those flags and are discovered once
     * per chain using Spring's default classloader policy: the thread context
     * classloader first, falling back to Spring's defining classloader. This
     * preserves application-provider discovery in executable jars and child
     * application classloaders.
     */
    public DataSourceUnwrapperChain(boolean p6spy, boolean seata) {
        if (seata) {
            unwrappers.add(new SeataDataSourceUnwrapper());
        }
        if (p6spy) {
            unwrappers.add(new P6SpyDataSourceUnwrapper());
        }
        try {
            for (DataSourceUnwrapper unwrapper : ServiceLoader.load(
                    DataSourceUnwrapper.class, ClassUtils.getDefaultClassLoader())) {
                unwrappers.add(unwrapper);
            }
        } catch (ServiceConfigurationError e) {
            throw new IllegalStateException("Cannot load datasource unwrappers", e);
        }
        AnnotationAwareOrderComparator.sort(unwrappers);
    }

    /**
     * Returns the first successfully unwrapped target, or the input unchanged.
     * Only one layer is removed, preserving the routing datasource's lifecycle
     * behavior for nested proxies. Failures propagate to the caller for logging.
     */
    public DataSource unwrap(DataSource dataSource) throws Exception {
        if (dataSource != null) {
            for (DataSourceUnwrapper unwrapper : unwrappers) {
                if (unwrapper.support(dataSource)) {
                    DataSource target = unwrapper.unwrap(dataSource);
                    if (target != null && target != dataSource) {
                        return target;
                    }
                }
            }
        }
        return dataSource;
    }
}
