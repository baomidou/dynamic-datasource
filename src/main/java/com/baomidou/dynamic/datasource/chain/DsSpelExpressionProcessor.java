package com.baomidou.dynamic.datasource.chain;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

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

    @Override
    public String doDetermineDatasource(MethodInvocation invocation, String key) {
        Method method = invocation.getMethod();
        Object[] arguments = invocation.getArguments();
        EvaluationContext context = new MethodBasedEvaluationContext(null, method, arguments, NAME_DISCOVERER);
        return PARSER.parseExpression(key).getValue(context).toString();
    }
}
