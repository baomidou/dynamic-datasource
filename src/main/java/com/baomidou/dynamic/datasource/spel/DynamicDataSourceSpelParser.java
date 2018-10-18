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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 动态数据源Spel解析器
 *
 * @author TaoYu
 * @since 2.3.0
 */
public class DynamicDataSourceSpelParser {

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    @Autowired(required = false)
    private HttpSession session;

    @Autowired(required = false)
    private HttpServletRequest request;

    /**
     * 解析多数据源spel的参数
     *
     * @param invocation 动态方法执行器
     * @param key        需要解析的key
     * @return 解析后的值
     */
    public String parse(MethodInvocation invocation, String key) {
        if (key.startsWith("#session")) {
            return session.getAttribute(key.split(".")[1]).toString();
        } else if (key.startsWith("#header")) {
            return request.getHeader(key.split(".")[1]);
        } else {
            EvaluationContext context = new MethodBasedEvaluationContext(null, invocation.getMethod(), invocation.getArguments(), NAME_DISCOVERER);
            return PARSER.parseExpression(key).getValue(context).toString();
        }
    }

}