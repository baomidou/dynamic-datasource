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
package com.baomidou.dynamic.datasource.fixture.v3.service.tx;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.*;


@Service
@DS("account")
public class AccountService {
    private final DataSource dataSource;

    public AccountService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void reduceBalance(Integer userId, Double price) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("select * from account where id=?");
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Account account = null;
            if (resultSet.next()) {
                Integer id = resultSet.getObject(1, Integer.class);
                Double balance = resultSet.getObject(2, Double.class);
                account = new Account(id, balance);
            }
            Assert.notNull(account, "用户不存在");
            Double balance = account.getBalance();
            if (balance < price) {
                throw new RuntimeException("余额不足");
            }
            double currentBalance = account.getBalance() - price;
            String sql = "update account set balance=? where id=?";
            PreparedStatement updateStatement = connection.prepareStatement(sql);
            updateStatement.setDouble(1, currentBalance);
            updateStatement.setInt(2, userId);
            updateStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Account selectAccount() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM account where id=1");
            Account account = null;
            if (resultSet.next()) {
                Integer id = resultSet.getObject(1, Integer.class);
                Double balance = resultSet.getObject(2, Double.class);
                account = new Account(id, balance);
            }
            return account;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
