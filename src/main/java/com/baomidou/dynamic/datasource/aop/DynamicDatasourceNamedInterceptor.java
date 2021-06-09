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
package com.baomidou.dynamic.datasource.aop;


import com.baomidou.dynamic.datasource.processor.DsProcessor;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.ClassUtils;
import org.springframework.util.PatternMatchUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Named Interceptor of Dynamic Datasource
 *
 * @author TaoYu
 * @since 3.4.0
 */
@Slf4j
public class DynamicDatasourceNamedInterceptor implements MethodInterceptor {

    private static final String DYNAMIC_PREFIX = "#";
    private final Map<String, String> nameMap = new HashMap<>();
    private final DsProcessor dsProcessor;

    public DynamicDatasourceNamedInterceptor(DsProcessor dsProcessor) {
        this.dsProcessor = dsProcessor;
    }

    @Nullable
    @Override
    public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
        String dsKey = determineDatasourceKey(invocation);
        DynamicDataSourceContextHolder.push(dsKey);
        try {
            return invocation.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * add Item Pattern
     *
     * @param methodName like select*
     * @param dsKey      like master or slave
     */
    public void addPattern(@Nonnull String methodName, @Nonnull String dsKey) {
        log.debug("dynamic-datasource adding ds method [" + methodName + "] with attribute [" + dsKey + "]");
        nameMap.put(methodName, dsKey);
    }

    /**
     * add PatternMap
     *
     * @param map namedMap
     */
    public void addPatternMap(Map<String, String> map) {
        for (Map.Entry<String, String> item : map.entrySet()) {
            addPattern(item.getKey(), item.getValue());
        }
    }

    /**
     * config from properties
     * <pre>
     *         Properties attributes = new Properties();
     *         attributes.setProperty("select*", "slave");
     *         attributes.setProperty("add*", "master");
     *         attributes.setProperty("update*", "master");
     *         attributes.setProperty("delete*", "master");
     * </pre>
     *
     * @param properties ds properties
     */
    public void fromProperties(@Nonnull Properties properties) {
        Enumeration<?> propNames = properties.propertyNames();
        while (propNames.hasMoreElements()) {
            String methodName = (String) propNames.nextElement();
            String value = properties.getProperty(methodName);
            this.addPattern(methodName, value);
        }
    }


    private boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }

    private String determineDatasourceKey(MethodInvocation invocation) {
        String key = findDsKey(invocation);
        return (key != null && key.startsWith(DYNAMIC_PREFIX)) ? dsProcessor.determineDatasource(invocation, key) : key;
    }

    private String findDsKey(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        if (!ClassUtils.isUserLevelMethod(method)) {
            return null;
        }

        // Look for direct name match.
        String methodName = method.getName();
        String dsKey = this.nameMap.get(methodName);

        if (dsKey == null) {
            // Look for most specific name match.
            String bestNameMatch = null;
            for (String mappedName : this.nameMap.keySet()) {
                if (isMatch(methodName, mappedName) &&
                        (bestNameMatch == null || bestNameMatch.length() <= mappedName.length())) {
                    dsKey = this.nameMap.get(mappedName);
                    bestNameMatch = mappedName;
                }
            }
        }
        return dsKey;
    }
}
