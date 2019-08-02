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

import com.baomidou.dynamic.datasource.matcher.ExpressionMatcher;
import com.baomidou.dynamic.datasource.matcher.Matcher;
import com.baomidou.dynamic.datasource.matcher.RegexMatcher;
import com.baomidou.dynamic.datasource.processor.DsProcessor;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.Ordered;

/**
 * @author TaoYu
 * @since 1.2.0
 */
public class DynamicDataSourceAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware {

  /**
   * The identification of SPEL
   */
  private static final String DYNAMIC_PREFIX = "#";

  @Setter
  private DsProcessor dsProcessor;

  private Advice advice;

  private Pointcut pointcut;

  private Map<String, String> matchesCache = new HashMap<String, String>();

  public DynamicDataSourceAdvisor(List<Matcher> matchers) {
    this.pointcut = buildPointcut(matchers);
    this.advice = buildAdvice();
  }

  private Advice buildAdvice() {
    return new MethodInterceptor() {
      @Override
      public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
          Method method = invocation.getMethod();
          String methodPath = invocation.getThis().getClass().getName() + "." + method.getName();
          String key = matchesCache.get(methodPath);
          if (key != null && !key.isEmpty() && key.startsWith(DYNAMIC_PREFIX)) {
            key = dsProcessor.determineDatasource(invocation, key);
          }
          DynamicDataSourceContextHolder.push(key);
          return invocation.proceed();
        } finally {
          DynamicDataSourceContextHolder.poll();
        }
      }
    };
  }

  @Override
  public Pointcut getPointcut() {
    return this.pointcut;
  }

  @Override
  public Advice getAdvice() {
    return this.advice;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    if (this.advice instanceof BeanFactoryAware) {
      ((BeanFactoryAware) this.advice).setBeanFactory(beanFactory);
    }
  }

  private Pointcut buildPointcut(List<Matcher> matchers) {
    ComposablePointcut composablePointcut = null;
    for (Matcher matcher : matchers) {
      if (matcher instanceof RegexMatcher) {
        RegexMatcher regexMatcher = (RegexMatcher) matcher;
        Pointcut pointcut = new DynamicJdkRegexpMethodPointcut(regexMatcher.getPattern(),
            regexMatcher.getDs(), matchesCache);
        if (composablePointcut == null) {
          composablePointcut = new ComposablePointcut(pointcut);
        } else {
          composablePointcut.union(pointcut);
        }
      } else {
        ExpressionMatcher expressionMatcher = (ExpressionMatcher) matcher;
        Pointcut pointcut = new DynamicAspectJExpressionPointcut(expressionMatcher.getExpression(),
            expressionMatcher.getDs(), matchesCache);
        if (composablePointcut == null) {
          composablePointcut = new ComposablePointcut(pointcut);
        } else {
          composablePointcut.union(pointcut);
        }
      }
    }
    return composablePointcut;
  }
}
