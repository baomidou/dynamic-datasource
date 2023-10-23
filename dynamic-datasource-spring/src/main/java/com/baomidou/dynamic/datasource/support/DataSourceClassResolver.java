/*
 * Copyright © 2018 organization baomidou
 *
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
 */
package com.baomidou.dynamic.datasource.support;

import com.baomidou.dynamic.datasource.annotation.BasicAttribute;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.tx.TransactionalInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodClassKey;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源解析器
 *
 * @author TaoYu
 * @since 2.3.0
 */
@Slf4j
public class DataSourceClassResolver {

    /**
     * 默认事务属性
     */
    private static final TransactionalInfo NULL_TRANSACTION_ATTRIBUTE = new TransactionalInfo() {
        @Override
        public String toString() {
            return "null";
        }
    };
    private static boolean mpEnabled = false;
    private static Field mapperInterfaceField;

    static {
        Class<?> proxyClass = null;
        try {
            proxyClass = Class.forName("com.baomidou.mybatisplus.core.override.MybatisMapperProxy");
        } catch (ClassNotFoundException e1) {
            try {
                proxyClass = Class.forName("com.baomidou.mybatisplus.core.override.PageMapperProxy");
            } catch (ClassNotFoundException e2) {
                try {
                    proxyClass = Class.forName("org.apache.ibatis.binding.MapperProxy");
                } catch (ClassNotFoundException ignored) {
                }
            }
        }
        if (proxyClass != null) {
            try {
                mapperInterfaceField = proxyClass.getDeclaredField("mapperInterface");
                mapperInterfaceField.setAccessible(true);
                mpEnabled = true;
            } catch (NoSuchFieldException e) {
                log.warn("Failed to init mybatis-plus support.");
            }
        }
    }

    /**
     * 缓存方法对应的数据源
     */
    private final Map<Object, String> dsCache = new ConcurrentHashMap<>();
    /**
     * 缓存事务信息
     */
    private final Map<Object, TransactionalInfo> dsTransactionalCache = new ConcurrentHashMap<>();
    private final boolean allowedPublicOnly;

    /**
     * 加入扩展, 给外部一个修改aop条件的机会
     *
     * @param allowedPublicOnly 只允许公共的方法, 默认为true
     */
    public DataSourceClassResolver(boolean allowedPublicOnly) {
        this.allowedPublicOnly = allowedPublicOnly;
    }

    /**
     * 从缓存获取数据
     *
     * @param method       方法
     * @param targetObject 目标对象
     * @return ds
     */
    public String findKey(Method method, Object targetObject, Class<? extends Annotation> annotation) {
        if (method.getDeclaringClass() == Object.class) {
            return "";
        }
        Object cacheKey = new MethodClassKey(method, targetObject.getClass());
        String ds = this.dsCache.get(cacheKey);
        if (ds == null) {
            BasicAttribute<String> dsOperation = computeDatasource(method, targetObject, annotation);
            if (dsOperation == null) {
                ds = "";
            } else {
                ds = dsOperation.getDataOperation();
            }
            this.dsCache.put(cacheKey, ds);
        }
        return ds;
    }

    /**
     * 从缓存获取事务属性
     *
     * @param method       方法
     * @param targetObject 目标对象
     * @return TransactionalInfo
     */
    public TransactionalInfo findTransactionalInfo(Method method, Object targetObject, Class<? extends Annotation> annotation) {
        if (method.getDeclaringClass() == Object.class) {
            return NULL_TRANSACTION_ATTRIBUTE;
        }
        Object cacheKey = new MethodClassKey(method, targetObject.getClass());
        TransactionalInfo dsTransactional = this.dsTransactionalCache.get(cacheKey);
        if (dsTransactional == null) {
            BasicAttribute<TransactionalInfo> dsTransactionalOperation = computeDatasource(method, targetObject, annotation);
            if (dsTransactionalOperation == null) {
                dsTransactional = NULL_TRANSACTION_ATTRIBUTE;
            } else {
                dsTransactional = dsTransactionalOperation.getDataOperation();
            }
            this.dsTransactionalCache.put(cacheKey, dsTransactional);
        }
        return dsTransactional;
    }

    /**
     * 查找注解的顺序
     * 1. 当前方法
     * 2. 桥接方法
     * 3. 当前类开始一直找到Object
     * 4. 支持mybatis-plus, mybatis-spring
     *
     * @param method       方法
     * @param targetObject 目标对象
     * @return ds
     */
    private <T> BasicAttribute<T> computeDatasource(Method method, Object targetObject, Class<? extends Annotation> annotation) {
        if (allowedPublicOnly && !Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        //1. 从当前方法接口中获取
        BasicAttribute<T> dsAttr = findDataSourceAttribute(method, annotation);
        if (dsAttr != null) {
            return dsAttr;
        }
        Class<?> targetClass = targetObject.getClass();
        Class<?> userClass = ClassUtils.getUserClass(targetClass);
        // JDK代理时,  获取实现类的方法声明.  method: 接口的方法, specificMethod: 实现类方法
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, userClass);

        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        //2. 从实现类的方法找
        dsAttr = findDataSourceAttribute(specificMethod, annotation);
        if (dsAttr != null) {
            return dsAttr;
        }
        // 从当前方法声明的类查找
        dsAttr = findDataSourceAttribute(userClass, annotation);
        if (dsAttr != null && ClassUtils.isUserLevelMethod(method)) {
            return dsAttr;
        }
        //since 3.4.1 从接口查找，只取第一个找到的
        for (Class<?> interfaceClazz : ClassUtils.getAllInterfacesForClassAsSet(userClass)) {
            dsAttr = findDataSourceAttribute(interfaceClazz, annotation);
            if (dsAttr != null) {
                return dsAttr;
            }
        }
        // 如果存在桥接方法
        if (specificMethod != method) {
            // 从桥接方法查找
            dsAttr = findDataSourceAttribute(method, annotation);
            if (dsAttr != null) {
                return dsAttr;
            }
            // 从桥接方法声明的类查找
            dsAttr = findDataSourceAttribute(method.getDeclaringClass(), annotation);
            if (dsAttr != null && ClassUtils.isUserLevelMethod(method)) {
                return dsAttr;
            }
        }
        return getDefaultDataSourceAttr(targetObject, annotation);
    }

    /**
     * 默认的获取数据源名称方式
     *
     * @param targetObject 目标对象
     * @return ds
     */
    private <T> BasicAttribute<T> getDefaultDataSourceAttr(Object targetObject, Class<? extends Annotation> annotation) {
        Class<?> targetClass = targetObject.getClass();
        // 如果不是代理类, 从当前类开始, 不断的找父类的声明
        if (!Proxy.isProxyClass(targetClass)) {
            Class<?> currentClass = targetClass;
            while (currentClass != Object.class) {
                BasicAttribute<T> datasourceAttr = findDataSourceAttribute(currentClass, annotation);
                if (datasourceAttr != null) {
                    return datasourceAttr;
                }
                currentClass = currentClass.getSuperclass();
            }
        }
        // mybatis-plus, mybatis-spring 的获取方式
        if (mpEnabled) {
            final Class<?> clazz = getMapperInterfaceClass(targetObject);
            if (clazz != null) {
                BasicAttribute<T> datasourceAttr = findDataSourceAttribute(clazz, annotation);
                if (datasourceAttr != null) {
                    return datasourceAttr;
                }
                // 尝试从其父接口获取
                return findDataSourceAttribute(clazz.getSuperclass(), annotation);
            }
        }
        return null;
    }

    /**
     * 用于处理嵌套代理
     *
     * @param target JDK 代理类对象
     * @return InvocationHandler 的 Class
     */
    private Class<?> getMapperInterfaceClass(Object target) {
        Object current = target;
        while (Proxy.isProxyClass(current.getClass())) {
            Object currentRefObject = AopProxyUtils.getSingletonTarget(current);
            if (currentRefObject == null) {
                break;
            }
            current = currentRefObject;
        }
        try {
            if (Proxy.isProxyClass(current.getClass())) {
                return (Class<?>) mapperInterfaceField.get(Proxy.getInvocationHandler(current));
            }
        } catch (IllegalAccessException ignore) {
        }
        return null;
    }

    /**
     * 通过 AnnotatedElement 查找标记的注解, 映射为BasicAttribute
     *
     * @param ae AnnotatedElement
     * @return 数据源映射持有者
     */
    @SuppressWarnings("unchecked")
    private <T> BasicAttribute<T> findDataSourceAttribute(AnnotatedElement ae, Class<? extends Annotation> annotation) {
        if (annotation.isAssignableFrom(DS.class)) {
            //AnnotatedElementUtils.findMergedAnnotation()会委托给findMergedAnnotationAttributes()
            DS ds = AnnotatedElementUtils.findMergedAnnotation(ae, DS.class);
            if (ds != null) {
                return new BasicAttribute(ds.value());
            }
        } else if (annotation.isAssignableFrom(DSTransactional.class)) {
            DSTransactional dsTransactional = AnnotatedElementUtils.findMergedAnnotation(ae, DSTransactional.class);
            if (dsTransactional != null) {
                TransactionalInfo transactionalInfo = new TransactionalInfo();
                transactionalInfo.setPropagation(dsTransactional.propagation());
                transactionalInfo.setRollbackFor(dsTransactional.rollbackFor());
                transactionalInfo.setNoRollbackFor(dsTransactional.noRollbackFor());
                return new BasicAttribute(transactionalInfo);
            }
        }
        return null;
    }
}