package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceAutoConfigure;
import com.alibaba.druid.spring.boot3.autoconfigure.properties.DruidStatProperties;
import com.alibaba.druid.spring.boot3.autoconfigure.stat.DruidFilterConfiguration;
import com.alibaba.druid.spring.boot3.autoconfigure.stat.DruidSpringAopConfiguration;
import com.alibaba.druid.spring.boot3.autoconfigure.stat.DruidStatViewServletConfiguration;
import com.alibaba.druid.spring.boot3.autoconfigure.stat.DruidWebStatFilterConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 从原生DruidDataSourceAutoConfigure复制
 *
 * @author TaoYu
 * @since 1.1.0
 */
@Configuration
@ConditionalOnClass(DruidDataSourceAutoConfigure.class)
@EnableConfigurationProperties({DruidStatProperties.class})
@Import({
        DruidSpringAopConfiguration.class,
        DruidStatViewServletConfiguration.class,
        DruidWebStatFilterConfiguration.class,
        DruidFilterConfiguration.class})
public class DruidDynamicDataSourceConfiguration {

}
