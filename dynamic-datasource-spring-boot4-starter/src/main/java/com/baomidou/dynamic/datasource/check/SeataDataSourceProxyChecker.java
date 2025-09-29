/*
 * Copyright © 2018 organization baomidou
 *
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
 */
package com.baomidou.dynamic.datasource.check;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = DynamicDataSourceProperties.PREFIX, name = "seata", havingValue = "true", matchIfMissing = false)
public class SeataDataSourceProxyChecker implements SmartInitializingSingleton {
    private final ApplicationContext applicationContext;
    private final Class<?> seataAdviceClass = loadSeataAdviceClass();

    public SeataDataSourceProxyChecker(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, DynamicRoutingDataSource> dataSourceMap = applicationContext.getBeansOfType(DynamicRoutingDataSource.class);
        for (Map.Entry<String, DynamicRoutingDataSource> entry : dataSourceMap.entrySet()) {
            String beanName = entry.getKey();
            DynamicRoutingDataSource ds = entry.getValue();
            if (isSeataProxyBean(ds)) {
                log.warn("Seata auto-agent detected: AbstractDeliveringDataSource (beanName: {}). " +
                        "Please disable seata.enableAutoDataSourceProxy to avoid unknown exceptions.", beanName);
            }
        }
    }

    private boolean isSeataProxyBean(DynamicRoutingDataSource bean) {
        if (seataAdviceClass == null || !AopUtils.isAopProxy(bean) || !(bean instanceof Advised)) {
            return false;
        }
        Advised advised = (Advised) bean;
        for (Advisor advisor : advised.getAdvisors()) {
            Advice advice = advisor.getAdvice();
            if (seataAdviceClass.isAssignableFrom(advice.getClass())) {
                return true;
            }
        }
        return false;
    }

    private static Class<?> loadSeataAdviceClass() {
        try {
            return Class.forName("io.seata.spring.annotation.datasource.SeataAutoDataSourceProxyAdvice");
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            log.debug("SeataAutoDataSourceProxyAdvice not found, skipping Seata proxy check.", e);
            return null;
        }
    }
}
