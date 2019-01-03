package com.baomidou.dynamic.datasource.chain;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class DsHeaderProcessor extends DsProcessor {

    /**
     * header开头
     */
    private static final String HEADER_PREFIX = "#header";

    public boolean matches(String key) {
        return key.startsWith(HEADER_PREFIX);
    }

    public String doDetermineDatasource(MethodInvocation invocation, String key) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getHeader(key.substring(8));
    }
}
