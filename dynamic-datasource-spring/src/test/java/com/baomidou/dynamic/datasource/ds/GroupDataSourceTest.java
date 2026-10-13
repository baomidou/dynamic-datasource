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
package com.baomidou.dynamic.datasource.ds;

import com.baomidou.dynamic.datasource.exception.CannotFindDataSourceException;
import com.baomidou.dynamic.datasource.strategy.LoadBalanceDynamicDataSourceStrategy;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * 组数据源单元测试：成员增删、大小维护，以及委托策略在成员间路由。
 *
 * @author TaoYu
 */
class GroupDataSourceTest {

    @Test
    void determineOnEmptyGroupShouldThrowNamedExceptionNotDivideByZero() {
        // 旧实现：空集合取模 → ArithmeticException: / by zero，错误信息与数据源毫无关联
        GroupDataSource group = new GroupDataSource("slave", new LoadBalanceDynamicDataSourceStrategy());
        assertThatThrownBy(group::determineDataSource)
                .isInstanceOf(CannotFindDataSourceException.class)
                .hasMessageContaining("slave");
    }

    @Test
    void addShouldReturnNullThenPreviousOnOverwrite() {
        GroupDataSource group = new GroupDataSource("g", new LoadBalanceDynamicDataSourceStrategy());
        DataSource first = mock(DataSource.class);
        DataSource second = mock(DataSource.class);

        assertThat(group.addDatasource("ds_1", first)).isNull();
        assertThat(group.addDatasource("ds_1", second)).isSameAs(first);
        assertThat(group.size()).isEqualTo(1);
    }

    @Test
    void removeShouldReturnRemovedInstanceAndUpdateSize() {
        GroupDataSource group = new GroupDataSource("g", new LoadBalanceDynamicDataSourceStrategy());
        DataSource ds = mock(DataSource.class);
        group.addDatasource("ds_1", ds);
        group.addDatasource("ds_2", mock(DataSource.class));

        assertThat(group.removeDatasource("ds_1")).isSameAs(ds);
        assertThat(group.size()).isEqualTo(1);
        assertThat(group.removeDatasource("ds_1")).isNull();
    }

    @Test
    void determineDataSourceShouldReturnRegisteredMember() {
        GroupDataSource group = new GroupDataSource("g", names -> names.get(0));
        DataSource ds = mock(DataSource.class);
        group.addDatasource("ds_1", ds);

        assertThat(group.determineDsKey()).isEqualTo("ds_1");
        assertThat(group.determineDataSource()).isSameAs(ds);
    }

    @Test
    void strategyShouldReceiveTheFullMembership() {
        List<String> captured = new ArrayList<>();
        GroupDataSource group = new GroupDataSource("g", names -> {
            captured.addAll(names);
            return names.get(0);
        });
        group.addDatasource("ds_1", mock(DataSource.class));
        group.addDatasource("ds_2", mock(DataSource.class));

        group.determineDsKey();
        assertThat(captured).containsExactlyInAnyOrder("ds_1", "ds_2");
    }

    @Test
    void loadBalanceShouldSpreadAcrossAllMembers() {
        GroupDataSource group = new GroupDataSource("g", new LoadBalanceDynamicDataSourceStrategy());
        group.addDatasource("ds_1", mock(DataSource.class));
        group.addDatasource("ds_2", mock(DataSource.class));
        group.addDatasource("ds_3", mock(DataSource.class));

        List<String> picks = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            picks.add(group.determineDsKey());
        }
        // 每个成员都被轮到过
        assertThat(picks).contains("ds_1", "ds_2", "ds_3");
    }
}
