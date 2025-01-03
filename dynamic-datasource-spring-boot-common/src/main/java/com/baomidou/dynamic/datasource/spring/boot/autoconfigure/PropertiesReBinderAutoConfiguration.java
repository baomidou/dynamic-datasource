package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.autoconfigure.ConfigurationPropertiesRebinderAutoConfiguration;
import org.springframework.cloud.context.properties.ConfigurationPropertiesBeans;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ConfigurationPropertiesRebinderAutoConfiguration.class)
public class PropertiesReBinderAutoConfiguration {

    @Bean
    public DynamicDatasourceConfigurationPropertiesReBinder dsConfigurationReBinder(ConfigurationPropertiesBeans configurationPropertiesBeans) {
        return new DynamicDatasourceConfigurationPropertiesReBinder(configurationPropertiesBeans);
    }
}
