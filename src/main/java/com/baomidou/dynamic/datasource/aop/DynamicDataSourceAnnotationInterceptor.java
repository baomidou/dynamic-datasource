/**
 * Copyright © 2018 organization 苞米豆
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
package com.baomidou.dynamic.datasource.aop;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.support.MybatisPlusResolver;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态数据源AOP核心拦截器
 *
 * @author TaoYu
 * @since 1.2.0
 */
public class DynamicDataSourceAnnotationInterceptor implements MethodInterceptor {

    /**
     * 缓存方法注解值
     */
    private static final Map<String, String> METHOD_CACHE = new HashMap<>();

    private boolean mpEnabled;

    private MybatisPlusResolver mybatisPlusResolver;

    public DynamicDataSourceAnnotationInterceptor(boolean mpEnabled) {
        this.mpEnabled = mpEnabled;
        if (mpEnabled) {
            mybatisPlusResolver = new MybatisPlusResolver();
        }
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            DynamicDataSourceContextHolder.setDataSourceLookupKey(determineDatasource(invocation));
            return invocation.proceed();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceLookupKey();
        }
    }

    private String determineDatasource(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();
        if (mpEnabled) {
            declaringClass = mybatisPlusResolver.targetClass(invocation);
        }
        String cacheName = declaringClass.getName() + "." + method.getName();
        if (METHOD_CACHE.containsKey(cacheName)) {
            return METHOD_CACHE.get(cacheName);
        } else {
            DS ds = method.isAnnotationPresent(DS.class) ? method.getAnnotation(DS.class)
                    : AnnotationUtils.findAnnotation(declaringClass, DS.class);
            METHOD_CACHE.put(cacheName, ds.value());
            return ds.value();
        }
    }

}