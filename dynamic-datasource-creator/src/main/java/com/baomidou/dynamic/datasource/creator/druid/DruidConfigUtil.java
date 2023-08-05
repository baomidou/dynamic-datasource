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

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.toolkit.DsConfigUtil;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
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

    private static final String FILTERS = "druid.filters";

    private static final String CONFIG_STR = "config";
    private static final String STAT_STR = "stat";

    private static final Map<String, PropertyDescriptor> CONFIG_DESCRIPTOR_MAP = DsConfigUtil.getPropertyDescriptorMap(DruidConfig.class);
    private static final Map<String, PropertyDescriptor> DATASOURCE_DESCRIPTOR_MAP = DsConfigUtil.getPropertyDescriptorMap(DruidDataSource.class);

    private static final Class<?> CLAZZ = DruidDataSource.class;

    /**
     * 根据全局配置和本地配置结合转换为Properties
     *
     * @param g 全局配置
     * @param c 当前配置
     * @return Druid配置
     */
    public static Properties mergeConfig(DruidConfig g, @NonNull DruidConfig c) {
        Properties properties = new Properties();
        for (Map.Entry<String, PropertyDescriptor> entry : CONFIG_DESCRIPTOR_MAP.entrySet()) {
            String key = entry.getKey();
            PropertyDescriptor descriptor = entry.getValue();
            Method readMethod = descriptor.getReadMethod();
            Class<?> returnType = readMethod.getReturnType();
            if (List.class.isAssignableFrom(returnType) || Set.class.isAssignableFrom(returnType) || Map.class.isAssignableFrom(returnType) || Properties.class.isAssignableFrom(returnType)) {
                continue;
            }
            try {
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
            } catch (Exception e) {
                log.warn("druid current could not set  [" + key + " ]", e);
            }
        }

        //filters单独处理，默认了stat
        String filters = getValue(g, c, "filters");
        if (filters == null) {
            filters = STAT_STR;
        }
        String publicKey = getValue(g, c, "publicKey");
        boolean configFilterExist = publicKey != null && publicKey.length() > 0;
        if (publicKey != null && publicKey.length() > 0 && !filters.contains(CONFIG_STR)) {
            filters += "," + CONFIG_STR;
        }
        properties.setProperty(FILTERS, filters);

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

    /**
     * @param g     全局配置
     * @param c     当前配置
     * @param field 字段
     * @return 字段值
     */
    public static String getValue(DruidConfig g, @NonNull DruidConfig c, String field) {
        PropertyDescriptor propertyDescriptor = CONFIG_DESCRIPTOR_MAP.get(field);
        if (propertyDescriptor == null) {
            return null;
        }
        Method method = propertyDescriptor.getReadMethod();
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

    /**
     * 设置DruidDataSource的值
     *
     * @param dataSource DruidDataSource
     * @param field      字段
     * @param g          全局配置
     * @param c          当前配置
     */
    public static void setValue(DruidDataSource dataSource, String field, DruidConfig g, DruidConfig c) {
        PropertyDescriptor descriptor = DATASOURCE_DESCRIPTOR_MAP.get(field);
        if (descriptor == null) {
            log.warn("druid current not support [" + field + " ]");
            return;
        }
        Method writeMethod = descriptor.getWriteMethod();
        if (writeMethod == null) {
            log.warn("druid current could not set  [" + field + " ]");
            return;
        }
        try {
            Method configReadMethod = CONFIG_DESCRIPTOR_MAP.get(field).getReadMethod();
            Object value = configReadMethod.invoke(c);
            if (value != null) {
                writeMethod.invoke(dataSource, value);
                return;
            }
            if (g != null) {
                value = configReadMethod.invoke(g);
                if (value != null) {
                    writeMethod.invoke(dataSource, value);
                }
            }
        } catch (Exception e) {
            log.warn("druid current  set  [" + field + " ] error");
        }
    }
}
