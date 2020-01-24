package com.baomidou.samples.seata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.samples.seata.common.OperationResponse;
import com.baomidou.samples.seata.dao.OrderDao;
import com.baomidou.samples.seata.entity.Orders;
import com.baomidou.samples.seata.common.OrderStatus;
import com.baomidou.samples.seata.service.OrderService;
import com.baomidou.samples.seata.service.AccountService;
import com.baomidou.samples.seata.service.ProductService;
import com.baomidou.samples.seata.dto.PlaceOrderRequest;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final OrderDao orderDao;

  private final AccountService accountService;

  private final ProductService productService;

  @DS("order")
  @Override
  @Transactional
  @GlobalTransactional
  public OperationResponse placeOrder(PlaceOrderRequest placeOrderRequest) {
    log.info("=============ORDER=================");
    DynamicDataSourceContextHolder.push("order");
    log.info("当前 XID: {}", RootContext.getXID());

    Integer amount = 1;
    Integer price = placeOrderRequest.getPrice();

    Orders orders = Orders.builder()
        .userId(placeOrderRequest.getUserId())
        .productId(placeOrderRequest.getProductId())
        .status(OrderStatus.INIT)
        .payAmount(price)
        .build();

    Integer saveOrderRecord = orderDao.insert(orders);

    log.info("保存订单{}", saveOrderRecord > 0 ? "成功" : "失败");

    // 扣减库存
    boolean operationStorageResult = productService.reduceStock(placeOrderRequest.getProductId(), amount);

    // 扣减余额
    boolean operationBalanceResult = accountService.reduceBalance(placeOrderRequest.getUserId(), price);

    log.info("=============ORDER=================");

    orders.setStatus(OrderStatus.SUCCESS);
    Integer updateOrderRecord = orderDao.updateById(orders);
    log.info("更新订单:{} {}", orders.getId(), updateOrderRecord > 0 ? "成功" : "失败");

    return OperationResponse.builder()
        .success(operationStorageResult && operationBalanceResult)
        .build();
  }
}