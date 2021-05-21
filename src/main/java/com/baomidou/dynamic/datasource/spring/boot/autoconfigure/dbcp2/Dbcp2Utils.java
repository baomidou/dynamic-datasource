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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.dbcp2;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Dbcp2工具类
 *
 * @author TaoYu
 * @since 3.3.6
 */
@Slf4j
@UtilityClass
public class Dbcp2Utils {

    /**
     * @param g    global config
     * @param item item config
     * @return BasicDataSource
     */
    public BasicDataSource createDataSource(Dbcp2Config g, Dbcp2Config item) {
        BasicDataSource dataSource = new BasicDataSource();
        Class<BasicDataSource> dataSourceClass = BasicDataSource.class;
        Class<Dbcp2Config> clazz = Dbcp2Config.class;
        Field[] fields = clazz.getDeclaredFields();
        for (Field f : fields) {
            try {
                String propertyName = f.getName();
                PropertyDescriptor pd = new PropertyDescriptor(propertyName, clazz);
                Method readMethod = pd.getReadMethod();

                Object value = readMethod.invoke(item);
                if (value == null) {
                    value = readMethod.invoke(g);
                }
                if (value != null) {
                    try {
                        Method writeMethod = new PropertyDescriptor(propertyName, dataSourceClass).getWriteMethod();
                        writeMethod.invoke(dataSource, value);
                    } catch (IntrospectionException e1) {
                        log.warn("dynamic-datasource create dbcp2 get into trouble,your dbcp2 not support " + propertyName);
                    }
                }

            } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
                log.warn("dynamic-datasource create dbcp2 get into trouble", e);
            }

        }
        return dataSource;
    }
}
