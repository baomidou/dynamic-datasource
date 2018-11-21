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
package com.baomidou.dynamic.datasource.aop;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import java.lang.reflect.Method;
import java.util.Map;

public class DynamicAspectJExpressionPointcut extends AspectJExpressionPointcut {
    private Map<String, String> matchesCache;

    private String ds;

    public DynamicAspectJExpressionPointcut(String expression, String ds, Map<String, String> matchesCache) {
        this.ds = ds;
        this.matchesCache = matchesCache;
        setExpression(expression);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass, boolean beanHasIntroductions) {
        boolean matches = super.matches(method, targetClass, beanHasIntroductions);
        if (matches) {
            matchesCache.put(targetClass.getName() + "." + method.getName(), ds);
        }
        return matches;
    }
}