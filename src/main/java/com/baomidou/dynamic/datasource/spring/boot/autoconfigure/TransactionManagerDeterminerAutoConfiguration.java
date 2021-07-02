package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import com.baomidou.dynamic.datasource.spring.ext.AnnotationTransactionAttributeSourceReplacer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dyu 2021/7/2 11:30
 */
@Configuration
@ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "assignTm", havingValue = "true")
public class TransactionManagerDeterminerAutoConfiguration {

    @Bean
    public AnnotationTransactionAttributeSourceReplacer attributeSourceReplacer() {
        return new AnnotationTransactionAttributeSourceReplacer();
    }
}
