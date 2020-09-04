package com.baomidou.dynamic.datasource.aop;

import com.baomidou.dynamic.datasource.ds.proxy.ConnectionFactory;
import com.baomidou.dynamic.datasource.ds.proxy.TransactionContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import java.util.UUID;

public class DynamicTransactionAdvisor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Boolean state = true;
        Object o;
        String xid = UUID.randomUUID().toString();
        TransactionContext.bind(xid);
        try {
            o = methodInvocation.proceed();
        } catch (Exception e) {
            state = false;
            throw e;
        } finally {
            ConnectionFactory.notify(xid, state);
            TransactionContext.remove();
        }
        return o;
    }
}
