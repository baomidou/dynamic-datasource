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
import com.baomidou.dynamic.datasource.util.DynamicDataSourceContextHolder;
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

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            String datasource = determineDatasource(invocation);
            DynamicDataSourceContextHolder.setDataSourceLookupKey(datasource);
            return invocation.proceed();
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceLookupKey();
        }
    }

    private String determineDatasource(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        String methodName = method.getName();
        if (METHOD_CACHE.containsKey(methodName)) {
            return METHOD_CACHE.get(methodName);
        } else {
            DS ds = method.isAnnotationPresent(DS.class) ? method.getAnnotation(DS.class)
                    : AnnotationUtils.findAnnotation(method.getDeclaringClass(), DS.class);
            if (ds.value().isEmpty()) {
                throw new RuntimeException("2.0版本必须配置每一个value");
            }
            METHOD_CACHE.put(methodName, ds.value());
            return ds.value();
        }
    }

}