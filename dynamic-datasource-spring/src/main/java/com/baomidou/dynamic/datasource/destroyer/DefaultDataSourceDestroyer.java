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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Description
 * DefaultDataSourceDestroyer, support check hikari、druid and dhcp2
 *
 * @author alvinkwok
 * @since 2023/10/18
 */
@Slf4j
public class DefaultDataSourceDestroyer implements DataSourceDestroyer {

    private static final String THREAD_NAME = "close-datasource";

    private static final long TIMEOUT_CLOSE = 10 * 1000;

    private final List<DataSourceActiveDetector> detectors = new LinkedList<>();

    public DefaultDataSourceDestroyer() {
        detectors.add(new HikariDataSourceActiveDetector());
        detectors.add(new DruidDataSourceActiveDetector());
        detectors.add(new Dhcp2DataSourceActiveDetector());
    }


    public void asyncDestroy(String name, DataSource dataSource) {
        log.info("dynamic-datasource start asynchronous task to close the datasource named [{}],", name);
        ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName(THREAD_NAME);
            return thread;
        });
        executor.execute(() -> graceDestroy(name, dataSource));
        executor.shutdown();
    }

    private void graceDestroy(String name, DataSource dataSource) {
        try {
            DataSourceActiveDetector detector = detectors.stream()
                    .filter(x -> x.support(dataSource))
                    .findFirst()
                    .orElse(null);
            long start = System.currentTimeMillis();
            while (detector == null || detector.containsActiveConnection(dataSource)) {
                // make sure the datasource close
                if (System.currentTimeMillis() - start > TIMEOUT_CLOSE) {
                    break;
                }
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            log.warn("dynamic-datasource check the datasource named [{}] contains active connection failed,", name, e);
        }
        destroy(name, dataSource);
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
                log.info("dynamic-datasource close the datasource named [{}] success,", name);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("dynamic-datasource close the datasource named [{}] failed,", name, e);
        }
    }
}
