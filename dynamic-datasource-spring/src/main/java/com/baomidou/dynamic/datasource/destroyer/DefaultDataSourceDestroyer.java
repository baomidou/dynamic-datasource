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
package com.baomidou.dynamic.datasource.destroyer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description
 * DefaultDataSourceDestroyer,  support all sources
 *
 * @author alvinkwok
 * @since 2023/10/18
 */
@Slf4j
public class DefaultDataSourceDestroyer implements DataSourceDestroyer {

    private static final String THREAD_NAME = "close-db";

    public void asyncDestroy(String name, DataSource dataSource) {
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName(THREAD_NAME);
            return thread;
        });
        log.warn("dynamic-datasource async close the datasource named [{}],", name);
        executor.execute(() -> {
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            destroy(name, dataSource);
        });
        executor.shutdown();
    }

    /**
     * Immediately destroy the data source
     *
     * @param realDataSource wait destroy data source
     */
    public void destroy(String name, DataSource realDataSource) {
        Class<? extends DataSource> clazz = realDataSource.getClass();
        try {
            Method closeMethod = ReflectionUtils.findMethod(clazz, "close");
            if (closeMethod != null) {
                closeMethod.invoke(realDataSource);
                log.info("dynamic-datasource close the datasource named [{}]  success,", name);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("dynamic-datasource close the datasource named [{}] failed,", name, e);
        }
    }
}
