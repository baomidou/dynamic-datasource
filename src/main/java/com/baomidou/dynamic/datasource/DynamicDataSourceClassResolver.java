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
package com.baomidou.dynamic.datasource;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * 获取对mybatis-plus的支持
 *
 * @author TaoYu
 * @since 2.3.0
 */
@Slf4j
public class DynamicDataSourceClassResolver {

    private boolean mpEnabled = false;

    private Field mapperInterfaceField;

    public DynamicDataSourceClassResolver() {
        Class<?> proxyClass = null;
        try {
            proxyClass = Class.forName("com.baomidou.mybatisplus.core.override.PageMapperProxy");
        } catch (ClassNotFoundException e) {
            try {
                proxyClass = Class.forName("org.apache.ibatis.binding.MapperProxy");
            } catch (ClassNotFoundException e1) {
            }
        }
        if (proxyClass != null) {
            try {
                mapperInterfaceField = proxyClass.getDeclaredField("mapperInterface");
                mapperInterfaceField.setAccessible(true);
                mpEnabled = true;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    public Class<?> targetClass(MethodInvocation invocation) throws IllegalAccessException {
        if (mpEnabled) {
            Object target = invocation.getThis();
            Class<?> targetClass = target.getClass();
            return Proxy.isProxyClass(targetClass) ? (Class) mapperInterfaceField.get(Proxy.getInvocationHandler(target)) : targetClass;
        }
        return invocation.getMethod().getDeclaringClass();
    }
}
