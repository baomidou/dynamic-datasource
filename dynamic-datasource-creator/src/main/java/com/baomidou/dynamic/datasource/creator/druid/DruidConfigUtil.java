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

import com.baomidou.dynamic.datasource.toolkit.DsConfigUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

/**
 * Druid配置工具类
 *
 * @author TaoYu
 * @since 4.0.0
 */
@Slf4j
public final class DruidConfigUtil {

    private static final Map<String, Method> METHODS = DsConfigUtil.getGetterMethods(DruidConfig.class);

    /**
     * 根据全局配置和本地配置结合转换为Properties
     *
     * @param g 全局配置
     * @param c 当前配置
     * @return Druid配置
     */
    @SneakyThrows
    public static Properties mergeConfig(DruidConfig g, DruidConfig c) {
        Properties properties = new Properties();
        for (Map.Entry<String, Method> entry : METHODS.entrySet()) {
            String key = entry.getKey();
            Method readMethod = entry.getValue();
            Object cValue = readMethod.invoke(c);
            if (cValue != null) {
                properties.setProperty("druid." + key, String.valueOf(cValue));
                continue;
            }
            if (g != null) {
                Object gValue = readMethod.invoke(g);
                if (gValue != null) {
                    properties.setProperty("druid." + key, String.valueOf(gValue));
                }
            }
        }

        //filters单独处理，默认了stat
//        String filters = this.filters == null ? g.getFilters() : this.filters;
//        if (filters == null) {
//            filters = DruidConsts.STAT_STR;
//        }
//        if (publicKey != null && publicKey.length() > 0 && !filters.contains(DruidConsts.CONFIG_STR)) {
//            filters += "," + DruidConsts.CONFIG_STR;
//        }
//        properties.setProperty(DruidConsts.FILTERS, filters);
//
//        Properties connectProperties = connectionProperties == null ? g.getConnectionProperties() : connectionProperties;
//
//        if (publicKey != null && publicKey.length() > 0) {
//            if (connectProperties == null) {
//                connectProperties = new Properties();
//            }
//            connectProperties.setProperty("config.decrypt", Boolean.TRUE.toString());
//            connectProperties.setProperty("config.decrypt.key", publicKey);
//        }
//        this.connectionProperties = connectProperties;

        return properties;
    }
}