package com.baomidou.samples.mybatis.support;

import com.baomidou.dynamic.datasource.aop.DynamicDataSourceAnnotationInterceptor;
import com.baomidou.dynamic.datasource.processor.DsProcessor;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AllArgsConstructor
@EnableConfigurationProperties(DynamicDataSourceProperties.class)
public class AppConfiguration {



    @Bean
    public DynamicDataSourceAnnotationExtAdvisor dynamicDatasourceAnnotationAdvisor(DsProcessor dsProcessor,
                                                                                    DynamicDataSourceProperties properties) {
        DynamicDataSourceAnnotationInterceptor interceptor = new DynamicDataSourceAnnotationInterceptor();
        interceptor.setDsProcessor(dsProcessor);
        DynamicDataSourceAnnotationExtAdvisor advisor = new DynamicDataSourceAnnotationExtAdvisor(interceptor);
        advisor.setOrder(properties.getOrder());
        return advisor;
    }
}
