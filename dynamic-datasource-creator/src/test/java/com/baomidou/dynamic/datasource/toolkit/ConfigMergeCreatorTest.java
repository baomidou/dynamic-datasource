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
package com.baomidou.dynamic.datasource.toolkit;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ConfigMergeCreator} 合并全局配置与单个数据源配置的单元测试。
 * <p>
 * 连接池数据源创建器用无参构造器创建时，全局配置为 {@code null}。此前合并时读取全局配置的属性会抛出
 * {@link NullPointerException}（#821），现在 {@code null} 等同于空的全局配置。
 */
class ConfigMergeCreatorTest {

    private static final ConfigMergeCreator<PoolConfig, PoolTarget> MERGE_CREATOR =
            new ConfigMergeCreator<>("Test", PoolConfig.class, PoolTarget.class);

    /**
     * 只应用单个数据源已设置的属性，未设置的属性保留目标类自己的默认值。
     */
    @Test
    void nullGlobalShouldApplyOnlyTheItem() {
        PoolConfig item = new PoolConfig();
        item.setTestQuery("SELECT 1");
        item.setProperties(properties("item-key", "item-value"));

        PoolTarget result = MERGE_CREATOR.create(null, item);

        assertThat(result.getMaxSize()).isEqualTo(PoolTarget.DEFAULT_MAX_SIZE);
        assertThat(result.getTestQuery()).isEqualTo("SELECT 1");
        assertThat(result.getProperties()).isEqualTo(properties("item-key", "item-value"));
    }

    /**
     * 无参构造器创建的数据源创建器应与传入空的全局配置时一样（#821）。
     */
    @Test
    void nullGlobalShouldBehaveLikeAnEmptyGlobal() {
        PoolConfig item = new PoolConfig();
        item.setMaxSize(5);
        item.setProperties(properties("item-key", "item-value"));

        assertThat(MERGE_CREATOR.create(null, item))
                .usingRecursiveComparison()
                .isEqualTo(MERGE_CREATOR.create(new PoolConfig(), item));
    }

    /**
     * 全局配置不为 {@code null} 时行为不变：单个数据源已设置的属性优先，其余取全局配置；
     * {@link Properties} 按键合并，同名的键以单个数据源为准。
     */
    @Test
    void itemShouldOverrideTheGlobalWhichFillsTheRest() {
        PoolConfig global = new PoolConfig();
        global.setMaxSize(20);
        global.setTestQuery("SELECT 1");
        global.setProperties(properties("shared-key", "global-value", "global-key", "global-value"));
        PoolConfig item = new PoolConfig();
        item.setMaxSize(5);
        item.setProperties(properties("shared-key", "item-value"));

        PoolTarget result = MERGE_CREATOR.create(global, item);

        assertThat(result.getMaxSize()).isEqualTo(5);
        assertThat(result.getTestQuery()).isEqualTo("SELECT 1");
        assertThat(result.getProperties())
                .isEqualTo(properties("shared-key", "item-value", "global-key", "global-value"));
    }

    private static Properties properties(String... keyValues) {
        Properties properties = new Properties();
        for (int i = 0; i < keyValues.length; i += 2) {
            properties.setProperty(keyValues[i], keyValues[i + 1]);
        }
        return properties;
    }

    /**
     * 自己的配置类：与各连接池的配置类一样，属性都是包装类型，未设置时为 {@code null}。
     */
    @Getter
    @Setter
    public static class PoolConfig {
        private Integer maxSize;
        private String testQuery;
        private Properties properties;
    }

    /**
     * 目标配置类：与连接池自己的配置类一样带有默认值。
     */
    @Getter
    @Setter
    public static class PoolTarget {
        static final int DEFAULT_MAX_SIZE = 10;
        private int maxSize = DEFAULT_MAX_SIZE;
        private String testQuery;
        private Properties properties;
    }
}
