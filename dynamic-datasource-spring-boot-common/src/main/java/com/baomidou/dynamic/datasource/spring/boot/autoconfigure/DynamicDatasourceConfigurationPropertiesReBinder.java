package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import org.springframework.cloud.context.properties.ConfigurationPropertiesBeans;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A compensation mechanism used to support dynamic updates
 * {@link ConfigurationPropertiesBeans } loaded after {@link DynamicDataSourceProperties}
 * @see org.springframework.cloud.context.properties.ConfigurationPropertiesRebinder
 *
 * @author yuhuangbin
 */
public class DynamicDatasourceConfigurationPropertiesReBinder implements ApplicationListener<ContextRefreshedEvent> {

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final ConfigurationPropertiesBeans configurationPropertiesBeans;

    public DynamicDatasourceConfigurationPropertiesReBinder(ConfigurationPropertiesBeans configurationPropertiesBeans) {
        this.configurationPropertiesBeans = configurationPropertiesBeans;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!started.compareAndSet(false, true)) {
            return;
        }
        ApplicationContext applicationContext = event.getApplicationContext();
        DynamicDataSourceProperties aopProperties = applicationContext.getBean(DynamicDataSourceProperties.class);
        configurationPropertiesBeans.postProcessBeforeInitialization(aopProperties, aopProperties.getBeanName());
    }
}
