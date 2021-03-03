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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ls9527
 */
public class HealthCheckAdapter {

    /**
     * 维护数据源健康状况
     */
    private static final Map<String, Boolean> DB_HEALTH = new ConcurrentHashMap<>();

    public void putHealth(String key, Boolean healthState) {
        DB_HEALTH.put(key, healthState);
    }

    /**
     * 获取数据源连接健康状况
     *
     * @param dataSource 数据源名称
     * @return 健康状况
     */
    public boolean getHealth(String dataSource) {
        Boolean isHealth = DB_HEALTH.get(dataSource);
        return isHealth != null && isHealth;
    }
}
