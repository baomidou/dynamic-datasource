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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

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

    /**
     * 根据全局配置和本地配置结合转换为Properties
     *
     * @param config 当前配置
     * @return Druid配置
     */
    public static Properties toProperties(@NonNull DruidConfig config) {
        Properties properties = new Properties();
        for (Map.Entry<String, PropertyDescriptor> entry : CONFIG_DESCRIPTOR_MAP.entrySet()) {
            String key = entry.getKey();
            PropertyDescriptor descriptor = entry.getValue();
            Method readMethod = descriptor.getReadMethod();
            Class<?> returnType = readMethod.getReturnType();
            if (List.class.isAssignableFrom(returnType)
                    || Set.class.isAssignableFrom(returnType)
                    || Map.class.isAssignableFrom(returnType)
                    || Properties.class.isAssignableFrom(returnType)) {
                continue;
            }
            try {
                Object cValue = readMethod.invoke(config);
                if (cValue != null) {
                    properties.setProperty("druid." + key, String.valueOf(cValue));
                }
            } catch (Exception e) {
                log.warn("druid current could not set  [" + key + " ]", e);
            }
        }

        //filters单独处理，默认了stat
        String filters = config.getFilters();
        if (filters == null) {
            filters = STAT_STR;
        }
        String publicKey = config.getPublicKey();
        boolean configFilterExist = publicKey != null && !publicKey.isEmpty();
        if (publicKey != null && !publicKey.isEmpty() && !filters.contains(CONFIG_STR)) {
            filters += "," + CONFIG_STR;
        }
        properties.setProperty(FILTERS, filters);

        Properties connectProperties = Optional.ofNullable(config.getConnectionProperties())
                .orElse(new Properties());
        if (configFilterExist) {
            connectProperties.setProperty("config.decrypt", Boolean.TRUE.toString());
            connectProperties.setProperty("config.decrypt.key", publicKey);
        }
        config.setConnectionProperties(connectProperties);
        return properties;
    }

    /**
     * 设置DruidDataSource的值
     *
     * @param dataSource DruidDataSource
     * @param field      字段
     * @param c          当前配置
     */
    public static void setValue(DruidDataSource dataSource, String field, DruidConfig c) {
        try {
            Method configReadMethod = CONFIG_DESCRIPTOR_MAP.get(field).getReadMethod();
            Object value = configReadMethod.invoke(c);
            if (value != null) {
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
                writeMethod.invoke(dataSource, value);
            }
        } catch (Exception e) {
            log.warn("druid current  set  [" + field + " ] error");
        }
    }

    @SneakyThrows
    public static void merge(DruidConfig global, DruidConfig item) {
        if (global == null) {
            return;
        }
        BeanInfo beanInfo = Introspector.getBeanInfo(DruidConfig.class, Object.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor pd : propertyDescriptors) {
            Class<?> propertyType = pd.getPropertyType();
            if (Properties.class == propertyType) {
                mergeProperties(global, item, pd);
            } else if (List.class == propertyType) {
                mergeList(global, item, pd);
            } else if (Map.class == propertyType) {
                mergeMap(global, item, pd);
            } else {
                mergeBasic(global, item, pd);
            }
        }
    }

    @SneakyThrows
    private static void mergeList(DruidConfig global, DruidConfig item, PropertyDescriptor pd) {
        Method readMethod = pd.getReadMethod();
        Method writeMethod = pd.getWriteMethod();
        List<Object> result = new ArrayList<>();
        List<Object> itemValue = (List) readMethod.invoke(item);
        List<Object> globalValue = (List) readMethod.invoke(global);
        if (globalValue != null) {
            result.addAll(globalValue);
        }
        if (itemValue != null) {
            result.addAll(itemValue);
        }
        writeMethod.invoke(item, result);
    }

    @SneakyThrows
    private static void mergeMap(DruidConfig global, DruidConfig item, PropertyDescriptor pd) {
        Method readMethod = pd.getReadMethod();
        Method writeMethod = pd.getWriteMethod();
        Map result = new HashMap();
        Map itemValue = (Map) readMethod.invoke(item);
        Map globalValue = (Map) readMethod.invoke(global);
        if (globalValue != null) {
            result.putAll(globalValue);
        }
        if (itemValue != null) {
            result.putAll(itemValue);
        }
        writeMethod.invoke(item, result);
    }

    @SneakyThrows
    private static void mergeProperties(DruidConfig global, DruidConfig item, PropertyDescriptor pd) {
        Method readMethod = pd.getReadMethod();
        Method writeMethod = pd.getWriteMethod();
        Properties itemValue = (Properties) readMethod.invoke(item);
        Properties globalValue = (Properties) readMethod.invoke(global);
        Properties properties = new Properties();
        if (globalValue != null) {
            properties.putAll(globalValue);
        }
        if (itemValue != null) {
            properties.putAll(itemValue);
        }
        if (!properties.isEmpty()) {
            writeMethod.invoke(item, properties);
        }
    }

    @SneakyThrows
    private static void mergeBasic(DruidConfig global, DruidConfig item, PropertyDescriptor pd) {
        Method readMethod = pd.getReadMethod();
        Method writeMethod = pd.getWriteMethod();
        Object itemValue = readMethod.invoke(item);
        if (itemValue == null) {
            Object globalValue = readMethod.invoke(global);
            if (globalValue != null) {
                writeMethod.invoke(item, globalValue);
            }
        }

    }
}
