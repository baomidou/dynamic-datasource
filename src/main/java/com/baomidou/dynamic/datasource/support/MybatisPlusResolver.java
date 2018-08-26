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

import com.baomidou.mybatisplus.core.override.PageMapperProxy;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * 对mybatis-plus的支持
 *
 * @author TaoYu
 * @since 2.1.0
 */
public class MybatisPlusResolver {

    private static Field field;

    static {
        try {
            field = PageMapperProxy.class.getDeclaredField("mapperInterface");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public Class<?> targetClass(MethodInvocation invocation) throws IllegalAccessException {
        Object target = invocation.getThis();
        return Proxy.isProxyClass(target.getClass()) ? (Class) field.get(Proxy.getInvocationHandler(target)) : target.getClass();
    }

}
