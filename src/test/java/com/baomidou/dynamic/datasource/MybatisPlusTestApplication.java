package com.baomidou.dynamic.datasource;

import com.baomidou.dynamic.datasource.interceptor.OutputInterceptor;
import com.baomidou.dynamic.datasource.sample.service.SampleService;
import com.baomidou.dynamic.datasource.sample.service.impl.SampleServiceImpl;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot Application
 * <p>
 *
 * @author junjun
 */
@SpringBootApplication
@Import({DynamicDataSourceAutoConfiguration.class, OutputInterceptor.class})
public class MybatisPlusTestApplication {

    @Bean
    public SampleService sampleService() {
        return new SampleServiceImpl();
    }
}
