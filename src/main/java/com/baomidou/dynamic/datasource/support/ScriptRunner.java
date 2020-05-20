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
package com.baomidou.dynamic.datasource.support;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * @author TaoYu
 * @since 2020/1/21
 */
@Slf4j
@AllArgsConstructor
public class ScriptRunner {

    /**
     * 错误是否继续
     */
    private boolean continueOnError;
    /**
     * 分隔符
     */
    private String separator;

    /**
     * 执行数据库脚本
     *
     * @param dataSource 连接池
     * @param location   脚本位置
     */
    public void runScript(DataSource dataSource, String location) {
        if (StringUtils.hasText(location)) {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setContinueOnError(continueOnError);
            populator.setSeparator(separator);
            if (location.startsWith("classpath:")) {
                location = location.substring(10);
            }
            ClassPathResource resource = new ClassPathResource(location);
            if (resource.exists()) {
                populator.addScript(resource);
                try {
                    DatabasePopulatorUtils.execute(populator, dataSource);
                } catch (Exception e) {
                    log.warn("execute sql error", e);
                }
            } else {
                log.warn("could not find schema or data file {}", location);
            }
        }
    }

}
