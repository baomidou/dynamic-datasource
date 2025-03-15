package com.baomidou.dynamic.datasource.aop;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.annotation.Master;
import com.baomidou.dynamic.datasource.annotation.Slave;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DynamicDataSourceMethodInterceptor implements MethodInterceptor {

    private final List<Class> ignoreAnnos = Arrays.asList(DS.class, Master.class, Slave.class, DSTransactional.class);

    /**
     * key=package,v=ds
     * <p>
     * check current method or class ds annotation is present
     */
    private String ds;

    public DynamicDataSourceMethodInterceptor(String ds) {
        this.ds = ds;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (!checkIfAnnoPresent(invocation)) {
            if (StringUtils.hasText(ds)) {
                try {
                    DynamicDataSourceContextHolder.push(ds);
                    return invocation.proceed();
                } finally {
                    DynamicDataSourceContextHolder.poll();
                }
            }
        }
        return invocation.proceed();
    }

    /**
     * check Annotation DS,Master,Slave,DSTransactional
     *
     * @param invocation
     * @return
     */
    private boolean checkIfAnnoPresent(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        Class clazz = method.getDeclaringClass();
        if (!CollectionUtils.isEmpty(ignoreAnnos)) {
            for (Class ignoreAnno : ignoreAnnos) {
                Annotation annotationClass = clazz.getAnnotation(ignoreAnno);
                if (Objects.nonNull(annotationClass)) {
                    return Boolean.TRUE;
                }
                Annotation annotationMethod = method.getAnnotation(ignoreAnno);
                if (Objects.nonNull(annotationMethod)) {
                    return Boolean.TRUE;
                }
            }
        }
        return Boolean.FALSE;
    }
}

