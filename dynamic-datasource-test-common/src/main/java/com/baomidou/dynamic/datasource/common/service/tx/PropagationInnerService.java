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
package com.baomidou.dynamic.datasource.common.service.tx;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.tx.DsPropagation;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 传播契约测试的内层服务：每个方法以指定的 {@link DsPropagation} 写一行 tx_probe。
 * 内层方法必须由外层服务（或其他 bean）经代理调用，同类自调用不会触发 {@code @DSTransactional} 切面。
 */
@Service
@DS("order")
public class PropagationInnerService {

    private final DataSource dataSource;

    public PropagationInnerService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @DSTransactional(propagation = DsPropagation.REQUIRED)
    public void requiredInsertThenThrow(String tag) {
        insertProbe(tag);
        throw new IllegalStateException("required-inner");
    }

    @DSTransactional(propagation = DsPropagation.REQUIRES_NEW)
    public void requiresNewInsert(String tag) {
        insertProbe(tag);
    }

    @DSTransactional(propagation = DsPropagation.REQUIRES_NEW)
    public void requiresNewInsertThenThrow(String tag) {
        insertProbe(tag);
        throw new IllegalStateException("requires-new-inner");
    }

    @DSTransactional(propagation = DsPropagation.NESTED)
    public void nestedInsert(String tag) {
        insertProbe(tag);
    }

    @DSTransactional(propagation = DsPropagation.NESTED)
    public void nestedInsertThenThrow(String tag) {
        insertProbe(tag);
        throw new IllegalStateException("nested-inner");
    }

    @DSTransactional(propagation = DsPropagation.NOT_SUPPORTED)
    public void notSupportedInsert(String tag) {
        insertProbe(tag);
    }

    @DSTransactional(propagation = DsPropagation.SUPPORTS)
    public void supportsInsert(String tag) {
        insertProbe(tag);
    }

    @DSTransactional(propagation = DsPropagation.MANDATORY)
    public void mandatoryInsert(String tag) {
        insertProbe(tag);
    }

    @DSTransactional(propagation = DsPropagation.NEVER)
    public void neverInsert(String tag) {
        insertProbe(tag);
    }

    /**
     * 跨库 REQUIRES_NEW：方法级 {@code @DS("product")} 覆盖类级 order，独立提交到 product 库。
     */
    @DS("product")
    @DSTransactional(propagation = DsPropagation.REQUIRES_NEW)
    public void requiresNewInsertToProduct(String tag) {
        insertProbe(tag);
    }

    private void insertProbe(String tag) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("insert into tx_probe(tag) values (?)")) {
            statement.setString(1, tag);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
