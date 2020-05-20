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
package com.baomidou.dynamic.datasource.processor;

import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * @author TaoYu
 * @since 2.5.0
 */
public class DsSpelExpressionProcessor extends DsProcessor {

  /**
   * 参数发现器
   */
  private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
  /**
   * Express语法解析器
   */
  private static final ExpressionParser PARSER = new SpelExpressionParser();

  @Override
  public boolean matches(String key) {
    return true;
  }

  /**
   * 解析上下文的模板
   * 对于默认不设置的情况下,从参数中取值的方式 #param1
   * 设置指定模板 ParserContext.TEMPLATE_EXPRESSION 后的取值方式: #{#param1}
   * issues: https://github.com/baomidou/dynamic-datasource-spring-boot-starter/issues/199
   */
  private ParserContext parserContext = null;

  @Override
  public String doDetermineDatasource(MethodInvocation invocation, String key) {
    Method method = invocation.getMethod();
    Object[] arguments = invocation.getArguments();
    EvaluationContext context = new MethodBasedEvaluationContext(null, method, arguments, NAME_DISCOVERER);
    final Object value = PARSER.parseExpression(key,parserContext).getValue(context);
    return value == null ? null : value.toString();
  }

  public void setParserContext(ParserContext parserContext) {
    this.parserContext = parserContext;
  }
}
