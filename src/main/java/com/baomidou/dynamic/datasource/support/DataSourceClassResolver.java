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

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.MethodClassKey;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ClassUtils;

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
                e.printStackTrace();
            }
        }
    }

    /**
     * 缓存方法对应的数据源
     */
    private final Map<Object, String> dsCache = new ConcurrentHashMap<>();
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
    public String findDSKey(Method method, Object targetObject) {
        if (method.getDeclaringClass() == Object.class) {
            return "";
        }
        Object cacheKey = new MethodClassKey(method, targetObject.getClass());
        String ds = this.dsCache.get(cacheKey);
        if (ds == null) {
            ds = computeDatasource(method, targetObject);
            if (ds == null) {
                ds = "";
            }
            this.dsCache.put(cacheKey, ds);
        }
        return ds;
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
    private String computeDatasource(Method method, Object targetObject) {
        if (allowedPublicOnly && !Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        Class<?> targetClass = targetObject.getClass();
        Class<?> userClass = ClassUtils.getUserClass(targetClass);
        // JDK代理时,  获取实现类的方法声明.  method: 接口的方法, specificMethod: 实现类方法
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, userClass);

        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        // 从当前方法查找
        String dsAttr = findDataSourceAttribute(specificMethod);
        if (dsAttr != null) {
            return dsAttr;
        }
        // 从当前方法声明的类查找
        dsAttr = findDataSourceAttribute(userClass);
        if (dsAttr != null && ClassUtils.isUserLevelMethod(method)) {
            return dsAttr;
        }
        //since 3.4.1 从接口查找，只取第一个找到的
        for (Class<?> interfaceClazz : ClassUtils.getAllInterfacesForClassAsSet(userClass)) {
            dsAttr = findDataSourceAttribute(interfaceClazz);
            if (dsAttr != null) {
                return dsAttr;
            }
        }
        // 如果存在桥接方法
        if (specificMethod != method) {
            // 从桥接方法查找
            dsAttr = findDataSourceAttribute(method);
            if (dsAttr != null) {
                return dsAttr;
            }
            // 从桥接方法声明的类查找
            dsAttr = findDataSourceAttribute(method.getDeclaringClass());
            if (dsAttr != null && ClassUtils.isUserLevelMethod(method)) {
                return dsAttr;
            }
        }
        return getDefaultDataSourceAttr(targetObject);
    }

    /**
     * 默认的获取数据源名称方式
     *
     * @param targetObject 目标对象
     * @return ds
     */
    private String getDefaultDataSourceAttr(Object targetObject) {
        Class<?> targetClass = targetObject.getClass();
        // 如果不是代理类, 从当前类开始, 不断的找父类的声明
        if (!Proxy.isProxyClass(targetClass)) {
            Class<?> currentClass = targetClass;
            while (currentClass != Object.class) {
                String datasourceAttr = findDataSourceAttribute(currentClass);
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
                String datasourceAttr = findDataSourceAttribute(clazz);
                if (datasourceAttr != null) {
                    return datasourceAttr;
                }
                // 尝试从其父接口获取
                return findDataSourceAttribute(clazz.getSuperclass());
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
     * 通过 AnnotatedElement 查找标记的注解, 映射为  DatasourceHolder
     *
     * @param ae AnnotatedElement
     * @return 数据源映射持有者
     */
    private String findDataSourceAttribute(AnnotatedElement ae) {
        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(ae, DS.class);
        if (attributes != null) {
            return attributes.getString("value");
        }
        return null;
    }
}
