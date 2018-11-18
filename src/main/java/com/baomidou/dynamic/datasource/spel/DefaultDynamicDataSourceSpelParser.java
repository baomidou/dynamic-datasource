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
package com.baomidou.dynamic.datasource.spel;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 动态数据源Spel解析器
 *
 * @author TaoYu
 * @since 2.3.0
 */
public class DefaultDynamicDataSourceSpelParser implements DynamicDataSourceSpelParser {

    /**
     * 参数发现器
     */
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    /**
     * Express语法解析器
     */
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    /**
     * session开头
     */
    private static final String SESSION_PREFIX = "#session";
    /**
     * header开头
     */
    private static final String HEADER_PREFIX = "#header";


    /**
     * 解析多数据源spel的参数
     *
     * @param invocation 动态方法执行器
     * @param key        需要解析的key
     * @return 解析后的值
     */
    @Override
    public String parse(MethodInvocation invocation, String key) {
        if (key.startsWith(SESSION_PREFIX)) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request.getSession().getAttribute(key.substring(9)).toString();
        } else if (key.startsWith(HEADER_PREFIX)) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request.getHeader(key.substring(8));
        } else {
            Method method = invocation.getMethod();
            Object[] arguments = invocation.getArguments();
            EvaluationContext context = new MethodBasedEvaluationContext(null, method, arguments, NAME_DISCOVERER);
            return PARSER.parseExpression(key).getValue(context).toString();
        }
    }
}