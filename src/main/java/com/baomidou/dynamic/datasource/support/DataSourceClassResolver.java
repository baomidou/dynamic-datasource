/**
 * Copyright © 2018 organization baomidou
 * <pre>
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
 * <pre/>
 */
package com.baomidou.dynamic.datasource.support;

import com.baomidou.dynamic.datasource.annotation.DS;
import io.seata.common.util.StringUtils;
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
 * 获取对mybatis-plus的支持
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
                } catch (ClassNotFoundException e3) {
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
     * 默认的获取数据源名称方式
     *
     * @param targetObject
     * @return
     */
    protected DatasourceHolder getDefaultDataSourceHolder(Object targetObject) {
        Class<?> targetClass = targetObject.getClass();
        // 如果不是代理类, 从当前类开始, 不断的找父类的声明
        if (!Proxy.isProxyClass(targetClass)) {
            Class<?> currentClass = targetClass;
            while (currentClass != Object.class) {
                DatasourceHolder datasourceHolder = findDataSourceAttribute(currentClass);
                if (datasourceHolder != null) {
                    return datasourceHolder;
                }
                currentClass = currentClass.getSuperclass();
            }
        }
        // mybatis-plus, mybatis-spring 的获取方式
        if (mpEnabled) {
            final Class<?> clazz = getMapperInterfaceClass(targetObject);
            if (clazz != null) {
                return findDataSourceAttribute(clazz);
            }
        }
        return null;
    }

    /**
     * 用于处理嵌套代理
     *
     * @param target JDK 代理类对象
     * @return InvocationHandler 的 Class
     * @throws IllegalAccessException
     */
    protected Class<?> getMapperInterfaceClass(Object target) {
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

    // 空的数据源持有者,  虽然目前@DS 只有一个属性, 相比用String存储, 不如用对象和@DS映射更合适
    private DatasourceHolder NULL_DS = new DatasourceHolder(StringUtils.EMPTY);

    /**
     * 从缓存获取数据
     *
     * @param method
     * @param targetObject
     * @return
     * @throws IllegalAccessException
     */
    public String findDSKey(Method method, Object targetObject) {
        if (method.getDeclaringClass() == Object.class) {
            return StringUtils.EMPTY;
        }
        Class<?> targetClass = targetObject.getClass();

        Object cacheKey = getCacheKey(method, targetClass);
        DatasourceHolder cached = this.dsCache.get(cacheKey);
        if (cached != null) {
            if (cached == NULL_DS) {
                return StringUtils.EMPTY;
            } else {
                return cached.getValue();
            }
        } else {
            DatasourceHolder dsAttr = computeDatasourceHolder(method, targetObject);
            if (dsAttr == null) {
                dsAttr = NULL_DS;
            }
            this.dsCache.put(cacheKey, dsAttr);
            return dsAttr.getValue();
        }


    }

    /**
     * 获取缓存key,  默认通过spring的方式
     *
     * @param method      方法
     * @param targetClass 方法声明的类
     * @return
     */
    protected Object getCacheKey(Method method, Class<?> targetClass) {
        return new MethodClassKey(method, targetClass);
    }

    /**
     * 缓存方法对应的数据源, 有了缓存才能更快
     */
    private final Map<Object, DatasourceHolder> dsCache = new ConcurrentHashMap<>();

    /**
     * 查找注解的顺序
     * 1. 从当前方法
     * - 方法声明的类
     * 2. 桥接方法
     * - 桥接方法声明的类
     * 3. 从当前类开始一直找到Object
     * 4. 支持mybatis-plus, mybatis-spring
     *
     * @param method      方法
     * @param targetObject 目标对象
     * @return
     */
    private DatasourceHolder computeDatasourceHolder(Method method, Object targetObject) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }
        Class<?> targetClass = targetObject.getClass();
        Class<?> userClass = ClassUtils.getUserClass(targetClass);

        Method specificMethod = ClassUtils.getMostSpecificMethod(method, userClass);

        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        // 从当前方法查找
        DatasourceHolder dsAttr = findDataSourceAttribute(specificMethod);
        if (dsAttr != null) {
            return dsAttr;
        }
        // 从当前方法声明的类查找
        dsAttr = findDataSourceAttribute(specificMethod.getDeclaringClass());
        if (dsAttr != null && ClassUtils.isUserLevelMethod(method)) {
            return dsAttr;
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

        return getDefaultDataSourceHolder(targetObject);
    }

    /**
     * 通过 AnnotatedElement 查找标记的注解, 映射为  DatasourceHolder
     *
     * @param ae
     * @return 数据源映射持有者
     */
    private DatasourceHolder findDataSourceAttribute(AnnotatedElement ae) {
        AnnotationAttributes attributes = AnnotatedElementUtils.getMergedAnnotationAttributes(ae, DS.class);
        if (attributes != null) {
            final String value = attributes.getString("value");
            return new DatasourceHolder(value);
        }
        return null;
    }

    /**
     * 第一期先隐藏这个实现类
     */
    protected class DatasourceHolder {
        private String value;

        public DatasourceHolder(final String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }
}
