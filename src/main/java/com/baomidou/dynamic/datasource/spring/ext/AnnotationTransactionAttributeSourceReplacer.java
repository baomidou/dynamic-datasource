package com.baomidou.dynamic.datasource.spring.ext;

import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAttributeSource;

import java.beans.PropertyDescriptor;

/**
 * @author dyu 2021/6/3 19:28
 */
@Slf4j
@Component
public class AnnotationTransactionAttributeSourceReplacer implements InstantiationAwareBeanPostProcessor, PriorityOrdered {

    private final DynamicDataSourceProperties dynamicDataSourceProperties;

    public AnnotationTransactionAttributeSourceReplacer(DynamicDataSourceProperties dynamicDataSourceProperties) {
        this.dynamicDataSourceProperties = dynamicDataSourceProperties;
    }

    @Override
    public Object postProcessBeforeInstantiation(Class<?> aClass, String s) throws BeansException {
        if (!dynamicDataSourceProperties.isAssignTm()) {
            return null;
        }
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
        return null;
    }

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
