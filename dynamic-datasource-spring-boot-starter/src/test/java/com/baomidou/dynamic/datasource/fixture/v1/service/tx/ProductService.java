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
package com.baomidou.dynamic.datasource.fixture.v1.service.tx;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.*;

@Service
@DS("product")
public class ProductService {
    private final DataSource dataSource;

    public ProductService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Double reduceStock(Integer productId, Integer amount) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("select * from product where id=?");
            preparedStatement.setInt(1, productId);
            ResultSet resultSet = preparedStatement.executeQuery();
            Product product = null;
            if (resultSet.next()) {
                Integer id = resultSet.getObject(1, Integer.class);
                Double price = resultSet.getObject(2, Double.class);
                Integer stock = resultSet.getObject(3, Integer.class);
                product = new Product(id, price, stock);
            }
            Assert.notNull(product, "商品不存在");
            Integer stock = product.getStock();
            if (stock < amount) {
                throw new RuntimeException("库存不足");
            }
            int currentStock = stock - amount;
            String sql = "update product set stock=? where id=?";
            PreparedStatement updateStatement = connection.prepareStatement(sql);
            updateStatement.setInt(1, currentStock);
            updateStatement.setInt(2, productId);
            updateStatement.executeUpdate();
            return product.getPrice() * amount;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Product selectProduct() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM product where id=1");
            Product product = null;
            if (resultSet.next()) {
                Integer id = resultSet.getObject(1, Integer.class);
                Double price = resultSet.getObject(2, Double.class);
                Integer stock = resultSet.getObject(3, Integer.class);
                product = new Product(id, price, stock);
            }
            return product;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
