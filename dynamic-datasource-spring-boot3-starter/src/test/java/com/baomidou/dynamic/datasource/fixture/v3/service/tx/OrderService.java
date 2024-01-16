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
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.dynamic.datasource.tx.TransactionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;

import javax.sql.DataSource;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;


@Service
@DS("order")
public class OrderService {
    private final AccountService accountService;
    private final ProductService productService;
    private final DataSource dataSource;

    public OrderService(AccountService accountService, ProductService productService, DataSource dataSource) {
        this.accountService = accountService;
        this.productService = productService;
        this.dataSource = dataSource;
    }

    @DSTransactional
    public int placeOrder(PlaceOrderRequest request) {
        TransactionContext.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    request.setOrderStatus(OrderStatus.FAIL);
                }
            }
        });
        TransactionContext.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    request.setOrderStatus(OrderStatus.FAIL);
                }
            }
        });
        TransactionContext.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                request.setOrderStatus(OrderStatus.SUCCESS);
            }
        });

        try (Connection connection = dataSource.getConnection()) {
            Integer userId = request.getUserId();
            Integer productId = request.getProductId();
            Integer amount = request.getAmount();
            String sql = "insert into p_order(user_id,product_id,amount) values (?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, productId);
            preparedStatement.setInt(3, amount);
            preparedStatement.executeUpdate();
            // 扣减库存并计算总价
            Double totalPrice = productService.reduceStock(productId, amount);
            // 扣减余额
            accountService.reduceBalance(userId, totalPrice);
            String updateSql = "update p_order set total_price=? where user_id=? and product_id=?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            updateStatement.setDouble(1, totalPrice);
            updateStatement.setInt(2, userId);
            updateStatement.setInt(3, productId);
            updateStatement.executeUpdate();
            return request.getOrderStatus();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Order> selectOrders() {
        List<Order> result = new LinkedList<>();
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM p_order");
            while (resultSet.next()) {
                Integer id = resultSet.getObject(1, Integer.class);
                Integer userId = resultSet.getObject(2, Integer.class);
                Integer productId = resultSet.getObject(3, Integer.class);
                Integer amount = resultSet.getObject(4, Integer.class);
                Double totalPrice = resultSet.getObject(5, Double.class);
                Order order = new Order(id, userId, productId, amount, totalPrice);
                result.add(order);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
