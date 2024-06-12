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
package com.baomidou.dynamic.datasource.fixture.v1;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.fixture.v1.service.tx.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@SpringBootTest(classes = DsTransactionalApplication.class, webEnvironment = RANDOM_PORT)
public class DsTransactionalTest {
    @Autowired
    DataSource dataSource;
    @Autowired
    DefaultDataSourceCreator dataSourceCreator;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ProductService productService;
    private DynamicRoutingDataSource ds;

    @Test
    public void testDsTransactional() {
        DataSourceProperty orderDataSourceProperty = createDataSourceProperty("order");
        DataSourceProperty productDataSourceProperty = createDataSourceProperty("product");
        DataSourceProperty accountDataSourceProperty = createDataSourceProperty("account");
        ds = (DynamicRoutingDataSource) dataSource;
        ds.addDataSource(orderDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(orderDataSourceProperty));
        ds.addDataSource(productDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(productDataSourceProperty));
        ds.addDataSource(accountDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(accountDataSourceProperty));
        PlaceOrderRequest placeOrderRequest = new PlaceOrderRequest(1, 1, 22, OrderStatus.INIT);

        //商品不足
        assertThrows(RuntimeException.class, () -> orderService.placeOrder(placeOrderRequest));
        assertThat(placeOrderRequest.getOrderStatus()).isEqualTo(OrderStatus.FAIL);
        assertThat(orderService.selectOrders()).isEmpty();
        assertThat(accountService.selectAccount()).isEqualTo(new Account(1, 50.0));
        assertThat(productService.selectProduct()).isEqualTo(new Product(1, 10.0, 20));

        //账户不足
        placeOrderRequest.setAmount(6);
        placeOrderRequest.setOrderStatus(OrderStatus.INIT);
        assertThrows(RuntimeException.class, () -> orderService.placeOrder(placeOrderRequest));
        assertThat(placeOrderRequest.getOrderStatus()).isEqualTo(OrderStatus.FAIL);
        assertThat(orderService.selectOrders()).isEmpty();
        assertThat(accountService.selectAccount()).isEqualTo(new Account(1, 50.0));
        assertThat(productService.selectProduct()).isEqualTo(new Product(1, 10.0, 20));

        //正常下单
        placeOrderRequest.setAmount(5);
        placeOrderRequest.setOrderStatus(OrderStatus.INIT);
        assertThat(orderService.placeOrder(placeOrderRequest)).isEqualTo(OrderStatus.INIT);
        assertThat(placeOrderRequest.getOrderStatus()).isEqualTo(OrderStatus.SUCCESS);
        assertThat(orderService.selectOrders()).isEqualTo(Arrays.asList(new Order(3, 1, 1, 5, 50.0)));
        assertThat(accountService.selectAccount()).isEqualTo(new Account(1, 0.0));
        assertThat(productService.selectProduct()).isEqualTo(new Product(1, 10.0, 15));
    }

    private DataSourceProperty createDataSourceProperty(String poolName) {
        DataSourceProperty result = new DataSourceProperty();
        result.setPoolName(poolName);
        result.setDriverClassName("org.h2.Driver");
        result.setUrl("jdbc:h2:mem:" + poolName + ";INIT=RUNSCRIPT FROM 'classpath:db/ds-with-transactional.sql'");
        result.setUsername("sa");
        result.setPassword("");
        return result;
    }
}

@SpringBootApplication
class DsTransactionalApplication {
    public static void main(String[] args) {
        SpringApplication.run(DsTransactionalApplication.class, args);
    }
}
