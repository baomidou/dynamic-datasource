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
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
    public static Properties mergeConfig(DruidConfig g, @NonNull DruidConfig c) {
        Properties properties = new Properties();
        for (Map.Entry<String, Method> entry : METHODS.entrySet()) {
            String key = entry.getKey();
            Method readMethod = entry.getValue();
            Class<?> returnType = readMethod.getReturnType();
            if (List.class.isAssignableFrom(returnType) || Set.class.isAssignableFrom(returnType) || Map.class.isAssignableFrom(returnType) || Properties.class.isAssignableFrom(returnType)) {
                continue;
            }
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
        String filters = getValue(g, c, "filter");
        if (filters == null) {
            filters = DruidConsts.STAT_STR;
        }
        String publicKey = getValue(g, c, "publicKey");
        boolean configFilterExist = publicKey != null && publicKey.length() > 0;
        if (publicKey != null && publicKey.length() > 0 && !filters.contains(DruidConsts.CONFIG_STR)) {
            filters += "," + DruidConsts.CONFIG_STR;
        }
        properties.setProperty(DruidConsts.FILTERS, filters);

        Properties connectProperties = new Properties();
        Properties cConnectionProperties = c.getConnectionProperties();
        if (g != null) {
            Properties gConnectionProperties = g.getConnectionProperties();
            if (gConnectionProperties != null) {
                connectProperties.putAll(gConnectionProperties);
            }
        }
        if (cConnectionProperties != null) {
            connectProperties.putAll(cConnectionProperties);
        }
        if (configFilterExist) {
            connectProperties.setProperty("config.decrypt", Boolean.TRUE.toString());
            connectProperties.setProperty("config.decrypt.key", publicKey);
        }
        c.setConnectionProperties(connectProperties);
        return properties;
    }

    public static String getValue(DruidConfig g, @NonNull DruidConfig c, String field) {
        Method method = METHODS.get(field);
        if (method == null) {
            return null;
        }
        try {
            Object value = method.invoke(c);
            if (value != null) {
                return String.valueOf(value);
            }
            if (g != null) {
                value = method.invoke(g);
                if (value != null) {
                    return String.valueOf(value);
                }
            }
        } catch (Exception e) {
            // do nothing
        }
        return null;

    }
}