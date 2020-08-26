package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @Author: zs
 * @Date: 2020/8/24 18:33
 */
@Slf4j
public class DynamicTransactionProcessor implements BeanPostProcessor {

    private static final String TRANSACTION_MANAGER_SUFFIX = "ZSTransactionManager";


    @Override
    public Object postProcessBeforeInitialization(Object o, String beanName) throws BeansException {
        Class c = o.getClass();
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            enhanceBean(method);
        }
        return o;

    }

    private boolean enhanceBean(Method method) {
        //判断是否存在Transactional
        Transactional transactional = method.getAnnotation(Transactional.class);
        if (transactional != null) {
            //存在Transactional
            //动态修改值
            //获取 Transactional 这个代理实例所持有的 InvocationHandler
            InvocationHandler h = Proxy.getInvocationHandler(transactional);
            // 获取 AnnotationInvocationHandler 的 memberValues 字段
            Field hField = null;
            try {
                hField = h.getClass().getDeclaredField("memberValues");
                // 因为这个字段事 private final 修饰，所以要打开权限
                hField.setAccessible(true);
                // 获取 memberValues
                Map memberValues = (Map) hField.get(h);
                // 修改 value 属性值
                //获取DS 的值
                DS ds = method.getAnnotation(DS.class);
                if (ds != null) {
                    //TODO 如果别人设置了指定的manager那么不做任何处理
                    memberValues.put("transactionManager", ds.value() + TRANSACTION_MANAGER_SUFFIX);
                    return true;

                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                log.error("初始化TransactionaManager失败,返回原来的对象");
            }

        }
        return false;
    }


    @Override
    public Object postProcessAfterInitialization(Object o, String beanName) throws BeansException {
        return o;
    }


}

