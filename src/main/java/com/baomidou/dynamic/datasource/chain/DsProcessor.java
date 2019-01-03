package com.baomidou.dynamic.datasource.chain;

import org.aopalliance.intercept.MethodInvocation;

public abstract class DsProcessor {

    private DsProcessor nextProcessor;

    public void setNextProcessor(DsProcessor dsProcessor) {
        this.nextProcessor = dsProcessor;
    }

    public abstract boolean matches(String key);

    public String determineDatasource(MethodInvocation invocation, String key) {
        if (matches(key)) {
            String datasource = doDetermineDatasource(invocation, key);
            if (datasource != null && nextProcessor != null) {
                return nextProcessor.determineDatasource(invocation, key);
            }
            return datasource;
        }
        if (nextProcessor != null) {
            return nextProcessor.determineDatasource(invocation, key);
        }
        return null;
    }

    public abstract String doDetermineDatasource(MethodInvocation invocation, String key);
}
