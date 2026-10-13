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
package com.baomidou.dynamic.datasource.strategy;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 数据源选择策略单元测试：负载均衡的轮询确定性与随机策略的取值域。
 *
 * @author TaoYu
 */
class DynamicDataSourceStrategyTest {

    private final List<String> dsNames = Arrays.asList("ds_1", "ds_2", "ds_3");

    @Test
    void loadBalanceShouldRoundRobinInOrder() {
        LoadBalanceDynamicDataSourceStrategy strategy = new LoadBalanceDynamicDataSourceStrategy();
        assertThat(strategy.determineKey(dsNames)).isEqualTo("ds_1");
        assertThat(strategy.determineKey(dsNames)).isEqualTo("ds_2");
        assertThat(strategy.determineKey(dsNames)).isEqualTo("ds_3");
        // 到达末尾后回到开头，完成一轮
        assertThat(strategy.determineKey(dsNames)).isEqualTo("ds_1");
    }

    @Test
    void loadBalanceShouldCoverEveryDatasource() {
        LoadBalanceDynamicDataSourceStrategy strategy = new LoadBalanceDynamicDataSourceStrategy();
        Set<String> picked = new HashSet<>();
        for (int i = 0; i < dsNames.size(); i++) {
            picked.add(strategy.determineKey(dsNames));
        }
        assertThat(picked).containsExactlyInAnyOrderElementsOf(dsNames);
    }

    @Test
    void randomShouldOnlyReturnKnownDatasources() {
        RandomDynamicDataSourceStrategy strategy = new RandomDynamicDataSourceStrategy();
        Set<String> known = new HashSet<>(dsNames);
        for (int i = 0; i < 200; i++) {
            assertThat(known).contains(strategy.determineKey(dsNames));
        }
    }

    @Test
    void strategiesShouldHandleSingleDatasourceGroup() {
        List<String> single = Arrays.asList("only");
        assertThat(new LoadBalanceDynamicDataSourceStrategy().determineKey(single)).isEqualTo("only");
        assertThat(new RandomDynamicDataSourceStrategy().determineKey(single)).isEqualTo("only");
    }
}
