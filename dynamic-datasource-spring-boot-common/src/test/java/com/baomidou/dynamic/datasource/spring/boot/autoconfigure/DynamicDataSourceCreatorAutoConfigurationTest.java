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

import com.baomidou.dynamic.datasource.creator.atomikos.AtomikosDataSourceCreator;
import com.baomidou.dynamic.datasource.tx.AtomikosTransactionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DynamicDataSourceCreatorAutoConfiguration} 中 Atomikos 一组配置的装配条件（#811）。
 * <p>
 * mybatis-spring 是 compileOnly，classpath 上有 Atomikos 与 MyBatis 并不代表也有 mybatis-spring。
 * {@link FilteredClassLoader} 只对条件判断隐藏类，配置类本身仍由测试类加载器链接到 mybatis-spring：
 * 条件里漏掉 {@link SpringManagedTransactionFactory} 时，缺 mybatis-spring 的用例会看到事务工厂照常注册，而不是启动失败。
 */
class DynamicDataSourceCreatorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(DynamicDataSourceProperties.class)
            .withUserConfiguration(DynamicDataSourceCreatorAutoConfiguration.class);

    /**
     * Atomikos、MyBatis 与 mybatis-spring 都在时，创建器与 {@link AtomikosTransactionFactory} 照常注册。
     */
    @Test
    void atomikosBeansShouldBeRegisteredWithMybatisSpring() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(AtomikosDataSourceCreator.class);
            assertThat(context).getBean(TransactionFactory.class).isInstanceOf(AtomikosTransactionFactory.class);
        });
    }

    /**
     * 只有 Atomikos 与 MyBatis、没有 mybatis-spring 时，整组配置跳过，上下文照常启动。
     */
    @Test
    void atomikosBeansShouldBeSkippedWithoutMybatisSpring() {
        contextRunner.withClassLoader(new FilteredClassLoader(SpringManagedTransactionFactory.class)).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(AtomikosDataSourceCreator.class);
            assertThat(context).doesNotHaveBean(TransactionFactory.class);
        });
    }
}
