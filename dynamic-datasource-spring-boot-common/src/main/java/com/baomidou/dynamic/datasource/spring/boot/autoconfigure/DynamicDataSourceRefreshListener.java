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
package com.baomidou.dynamic.datasource.spring.boot.autoconfigure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 * Spring Cloud 配置刷新监听器：收到刷新事件后，把重新绑定好的数据源配置交给 {@link DynamicDataSourceRefresher} 落地。
 * <p>
 * 这里刻意以<b>字符串</b>比对事件类型而不是直接 {@code implements ApplicationListener<RefreshEvent>}：
 * 框架不绑定任何配置中心，也不该为了一个可选集成把 Spring Cloud 拖进编译期依赖，
 * 用类名匹配可以让 Boot 2 / 3 / 4 三条产线共用同一份实现而无需关心 Spring Cloud 版本。
 * 真正需要 {@code RefreshEvent} 存在的判断交给自动装配的 {@code @ConditionalOnClass}。
 *
 * @author TaoYu
 * @since 4.7.0
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicDataSourceRefreshListener implements SmartApplicationListener {

    /**
     * org.springframework.cloud.context.refresh.RefreshEvent
     */
    private static final String REFRESH_EVENT = "org.springframework.cloud.context.refresh.RefreshEvent";

    private final DynamicDataSourceRefresher refresher;
    private final DynamicDataSourceProperties properties;

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return REFRESH_EVENT.equals(eventType.getName());
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        log.debug("dynamic-datasource received {}, start refreshing datasource", event.getClass().getSimpleName());
        refresher.refresh(properties.getDatasource());
    }
}
