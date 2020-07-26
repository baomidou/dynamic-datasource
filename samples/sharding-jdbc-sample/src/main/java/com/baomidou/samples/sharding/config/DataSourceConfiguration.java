package com.baomidou.samples.sharding.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.provider.DynamicDataSourceProvider;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import org.apache.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@AutoConfigureBefore({DynamicDataSourceAutoConfiguration.class,
        SpringBootConfiguration.class})
public class DataSourceConfiguration {

    /**
     * 将动态数据源设置为主要的。
     * 当spring存在多个数据源时, 自动注入的是主要的对象
     * 设置为主要的数据源之后，就可以支持shardingjdbc原生的配置方式了
     * @return
     */
    @Primary
    @Bean
    public DataSource dataSource(DynamicDataSourceProperties properties,
                                 DynamicDataSourceProvider dynamicDataSourceProvider) {
        DynamicRoutingDataSource dataSource = new DynamicRoutingDataSource();
        dataSource.setPrimary(properties.getPrimary());
        dataSource.setStrict(properties.getStrict());
        dataSource.setStrategy(properties.getStrategy());
        dataSource.setProvider(dynamicDataSourceProvider);
        dataSource.setP6spy(properties.getP6spy());
        dataSource.setSeata(properties.getSeata());
        return dataSource;
    }

    /**
     * sharding-jdbc 提供4种数据源。
     * 1. sharding: 分片（主从，阴影，加密)只要存在一个就不注册sharding了
     * 2. masterslave: 主从
     * 3. shadow: 阴影？？
     * 4. encrypt: 脱敏
     * <p>
     * 目前支持的方式: sharding,masterslave. 不支持同时配置shadow 或者 encrypt.
     *
     * @param e 容器刷新事件
     */
    @EventListener
    public void shardingDataSourceListener(ContextRefreshedEvent e) {
        ApplicationContext applicationContext = e.getApplicationContext();
        DynamicRoutingDataSource dynamicRoutingDataSource = applicationContext.getBean(DynamicRoutingDataSource.class);
        Map<String, AbstractDataSourceAdapter> stringAbstractDataSourceAdapterMap =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, AbstractDataSourceAdapter.class, false, false);

        if (stringAbstractDataSourceAdapterMap.size() > 1) {
            //不支持
            logger.warn("不支持shardingjdbc的多种类型数据源的配置");
        } else if (stringAbstractDataSourceAdapterMap.size() == 1) {
            // 找到合适的数据源
            AbstractDataSourceAdapter shardingDataSource = stringAbstractDataSourceAdapterMap.values().iterator().next();
            dynamicRoutingDataSource.addDataSource("sharding", shardingDataSource);
        } else if (stringAbstractDataSourceAdapterMap.size() == 0) {
            //没有 sharding-jdbc, 不用管
            logger.warn("没有找到合适的shardingjdbc数据源");
        }

    }

    private static final Logger logger = LoggerFactory.getLogger(DataSourceConfiguration.class);
}
