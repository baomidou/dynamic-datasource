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
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 传播契约测试的读取端：在事务之外（无 xid）通过动态数据源直连，统计 tx_probe 中指定 tag
 * 已提交的行数，用于断言各传播行为落库与否。
 */
@Service
public class TxProbeQueryService {

    private final DataSource dataSource;

    public TxProbeQueryService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @DS("order")
    public int countOrder(String tag) {
        return count(tag);
    }

    @DS("product")
    public int countProduct(String tag) {
        return count(tag);
    }

    private int count(String tag) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("select count(*) from tx_probe where tag = ?")) {
            statement.setString(1, tag);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
