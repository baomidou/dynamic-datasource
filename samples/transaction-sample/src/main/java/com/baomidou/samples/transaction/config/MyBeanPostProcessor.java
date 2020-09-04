package com.baomidou.samples.transaction.config;

import com.baomidou.dynamic.datasource.transaction.DynamicDataSourceTransactionsFactory;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * @author Hccake 2020/9/4
 * @version 1.0
 */
@Component
@RequiredArgsConstructor
public class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof SqlSessionFactory){
            // 替换 SqlSessionFactory 的 TransactionsFactory
            SqlSessionFactory sqlSessionFactory = (SqlSessionFactory) bean;
            Configuration configuration = sqlSessionFactory.getConfiguration();
            Environment originEnvironment = configuration.getEnvironment();
            DataSource dataSource = originEnvironment.getDataSource();
            Environment environment = new Environment(originEnvironment.getId(),
                    new DynamicDataSourceTransactionsFactory(), dataSource);
            configuration.setEnvironment(environment);
        }
        return bean;
    }
}
