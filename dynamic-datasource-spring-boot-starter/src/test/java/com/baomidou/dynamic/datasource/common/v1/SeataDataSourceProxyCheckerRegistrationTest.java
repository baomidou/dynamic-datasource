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
package com.baomidou.dynamic.datasource.common.v1;

import com.baomidou.dynamic.datasource.check.SeataDataSourceProxyChecker;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceAutoConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 注册链路回归测试：#704 引入 {@link SeataDataSourceProxyChecker} 时未把它挂进自动装配注册文件，
 * 该类不在任何包扫描路径下，导致检查器静默失效。本测试锁定 spring.factories 中的注册声明，
 * 防止再次漏注册（检查器自身要由 @ConditionalOnProperty seata=true 激活，无法在本测试里构造运行环境）。
 */
public class SeataDataSourceProxyCheckerRegistrationTest {

    @Test
    void seataCheckerShouldBeRegisteredInSpringFactories() throws Exception {
        URL url = getClass().getClassLoader().getResource("META-INF/spring.factories");
        assertThat(url).as("spring.factories 必须存在于 boot2 starter 的构件中").isNotNull();
        Properties factories = new Properties();
        try (InputStream in = url.openStream()) {
            factories.load(in);
        }
        // 同时断言本模块的标志类，确保读到的是本模块的 spring.factories 而非依赖 jar 中的同名文件
        assertThat(factories.getProperty("org.springframework.boot.autoconfigure.EnableAutoConfiguration"))
                .contains(DynamicDataSourceAutoConfiguration.class.getName())
                .contains(SeataDataSourceProxyChecker.class.getName());
    }
}
