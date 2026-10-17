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
package com.baomidou.dynamic.datasource.common.base;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.common.service.tx.Account;
import com.baomidou.dynamic.datasource.common.service.tx.AccountService;
import com.baomidou.dynamic.datasource.common.service.tx.NonDatabaseConnectionService;
import com.baomidou.dynamic.datasource.common.service.tx.Order;
import com.baomidou.dynamic.datasource.common.service.tx.OrderService;
import com.baomidou.dynamic.datasource.common.service.tx.OrderStatus;
import com.baomidou.dynamic.datasource.common.service.tx.PlaceOrderRequest;
import com.baomidou.dynamic.datasource.common.service.tx.Product;
import com.baomidou.dynamic.datasource.common.service.tx.ProductService;
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

/**
 * {@code @DSTransactional} 跨多数据源本地事务的共享集成场景。
 * <p>
 * 版本无关：Boot2/3/4 的 starter 各以薄子类继承本类，跑的是同一套断言，只有加载的
 * Spring Boot 自动装配不同，从而真正验证多版本兼容性，避免逐份复制产生漂移。
 *
 * @author TaoYu
 */
@SpringBootTest(classes = DsTransactionalApplication.class, webEnvironment = RANDOM_PORT)
public abstract class AbstractDsTransactionalTest {

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
    @Autowired
    private NonDatabaseConnectionService nonDatabaseConnectionService;
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

    /**
     * 内层 {@code REQUIRES_NEW} 事务未持有 JDBC 连接时，外层提交不应抛 NPE。
     * 原仅存在于 Boot3，现上收为共享场景，Boot2/3/4 均回归验证。
     */
    @Test
    public void testRequiredWithRequiresNewNoConnection() {
        ds = (DynamicRoutingDataSource) dataSource;
        if (!ds.getDataSources().containsKey("order")) {
            DataSourceProperty orderDataSourceProperty = createDataSourceProperty("order");
            ds.addDataSource(orderDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(orderDataSourceProperty));
        }
        nonDatabaseConnectionService.outerRequiredWithConnection();
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

/**
 * 扫描范围不含各场景启动类所在的包，否则别的场景的启动类连同其 bean 会混进本场景的上下文（#812）。
 */
@SpringBootApplication(scanBasePackages = "com.baomidou.dynamic.datasource.common.service")
class DsTransactionalApplication {
    public static void main(String[] args) {
        SpringApplication.run(DsTransactionalApplication.class, args);
    }
}
