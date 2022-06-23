package com.baomidou.dynamic.datasource.support;

import org.springframework.core.Ordered;

/**
 * @author qw
 * @create 2022-05-05 23:09
 */
public abstract class DsTransactionSynchronization implements Ordered {
    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    public void beforeCompletion() {

    }

    public void afterCompletion(boolean state) {

    }

}
