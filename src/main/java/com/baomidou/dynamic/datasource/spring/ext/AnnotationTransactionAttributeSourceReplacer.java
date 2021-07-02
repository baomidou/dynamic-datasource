package com.baomidou.dynamic.datasource.spring.ext;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import java.beans.PropertyDescriptor;

/**
 * @author dyu 2021/6/3 19:28
 */
@Slf4j
public class AnnotationTransactionAttributeSourceReplacer implements InstantiationAwareBeanPostProcessor, PriorityOrdered {

    @Override
    public Object postProcessBeforeInstantiation(Class<?> aClass, String s) throws BeansException {
        log.trace(String.format("postProcessBeforeInstantiation - beanName: {%s}, beanClass: {%s})", s, aClass));
        if (s.equals("transactionAttributeSource") && TransactionAttributeSource.class.isAssignableFrom(aClass)) {
            log.debug("instantiating bean {} as {}", s, MergeAnnotationTransactionAttributeSource.class.getName());
            return new MergeAnnotationTransactionAttributeSource();
        } else {
            return null;
        }
    }

    @Override
    public boolean postProcessAfterInstantiation(Object o, String s) throws BeansException {
        return true;
    }

    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues propertyValues, PropertyDescriptor[] propertyDescriptors, Object o, String s) throws BeansException {
        return propertyValues;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
