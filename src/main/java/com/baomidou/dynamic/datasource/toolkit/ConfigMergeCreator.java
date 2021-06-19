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
package com.baomidou.dynamic.datasource.toolkit;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

@Slf4j
@AllArgsConstructor
public class ConfigMergeCreator<C, T> {

    private final String configName;

    private final Class<C> configClazz;

    private final Class<T> targetClazz;

    @SneakyThrows
    public T create(C global, C item) {
        T result = targetClazz.newInstance();
        BeanInfo beanInfo = Introspector.getBeanInfo(configClazz, Object.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor pd : propertyDescriptors) {
            Class<?> propertyType = pd.getPropertyType();
            if (Properties.class == propertyType) {
                mergeProperties(global, item, result, pd);
            } else {
                mergeBasic(global, item, result, pd);
            }
        }
        return result;
    }

    @SneakyThrows
    private void mergeProperties(C global, C item, T result, PropertyDescriptor pd) {
        String name = pd.getName();
        Method readMethod = pd.getReadMethod();
        Properties itemValue = (Properties) readMethod.invoke(item);
        Properties globalValue = (Properties) readMethod.invoke(global);
        Properties properties = new Properties();
        if (globalValue != null) {
            properties.putAll(globalValue);
        }
        if (itemValue != null) {
            properties.putAll(itemValue);
        }
        if (properties.size() > 0) {
            setField(result, name, properties);
        }
    }

    @SneakyThrows
    private void mergeBasic(C global, C item, T result, PropertyDescriptor pd) {
        String name = pd.getName();
        Method readMethod = pd.getReadMethod();
        Object value = readMethod.invoke(item);
        if (value == null) {
            value = readMethod.invoke(global);
        }
        if (value != null) {
            setField(result, name, value);
        }
    }

    private void setField(T result, String name, Object value) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(name, targetClazz);
            Method writeMethod = propertyDescriptor.getWriteMethod();
            writeMethod.invoke(result, value);
        } catch (IntrospectionException | ReflectiveOperationException e) {
            Field field = null;
            try {
                field = targetClazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(result, value);
            } catch (ReflectiveOperationException e1) {
                log.warn("dynamic-datasource set {} [{}] failed,please check your config or update {}  to the latest version", configName, name, configName);
            } finally {
                if (field != null && field.isAccessible()) {
                    field.setAccessible(false);
                }
            }
        } catch (Exception ee) {
            log.warn("dynamic-datasource set {} [{}] failed,please check your config", configName, name, ee);
        }
    }

}
