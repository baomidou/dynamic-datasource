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
package com.baomidou.dynamic.datasource.creator.druid;

import com.alibaba.druid.filter.logging.LogFilter;
import com.baomidou.dynamic.datasource.toolkit.DsConfigUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Druid日志配置工具类
 *
 * @author TaoYu
 * @since 3.5.0
 */
@Slf4j
public final class DruidLogConfigUtil {

    private static final Map<String, Method> METHODS = DsConfigUtil.getSetterMethods(LogFilter.class);

    /**
     * 根据当前的配置生成druid的日志filter
     *
     * @param clazz 日志类
     * @param map   配置
     * @return 日志filter
     */
    public static LogFilter initFilter(Class<? extends LogFilter> clazz, Map<String, Object> map) {
        try {
            LogFilter filter = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> item : map.entrySet()) {
                String key = DsConfigUtil.lineToUpper(item.getKey());
                Method method = METHODS.get(key);
                if (method != null) {
                    try {
                        method.invoke(filter, DsConfigUtil.convertValue(method, item.getValue()));
                    } catch (Exception e) {
                        log.warn("druid {} set param {} error", clazz.getName(), key, e);
                    }
                } else {
                    log.warn("druid {} does not have param {}", clazz.getName(), key);
                }
            }
            return filter;
        } catch (Exception e) {
            return null;
        }
    }
}