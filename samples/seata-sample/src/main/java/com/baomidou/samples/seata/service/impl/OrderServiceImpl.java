package com.baomidou.samples.seata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.seata.common.OrderStatus;
import com.baomidou.samples.seata.dao.OrderDao;
import com.baomidou.samples.seata.dto.PlaceOrderRequest;
import com.baomidou.samples.seata.entity.Order;
import com.baomidou.samples.seata.service.AccountService;
import com.baomidou.samples.seata.service.OrderService;
import com.baomidou.samples.seata.service.ProductService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Resource
    private OrderDao orderDao;
    @Autowired
    private AccountService accountService;
    @Autowired
    private ProductService productService;

    @DS("order")
    @Override
    @Transactional
    @GlobalTransactional
    public void placeOrder(PlaceOrderRequest request) {
        log.info("=============ORDER START=================");
        Long userId = request.getUserId();
        Long productId = request.getProductId();
        Integer amount = request.getAmount();
        log.info("收到下单请求,用户:{}, 商品:{},数量:{}", userId, productId, amount);

        log.info("当前 XID: {}", RootContext.getXID());

        Order order = Order.builder()
                .userId(userId)
                .productId(productId)
                .status(OrderStatus.INIT)
                .amount(amount)
                .build();

        orderDao.insert(order);
        log.info("订单一阶段生成，等待扣库存付款中");
        // 扣减库存并计算总价
        Double totalPrice = productService.reduceStock(productId, amount);
        // 扣减余额
        accountService.reduceBalance(userId, totalPrice);

        order.setStatus(OrderStatus.SUCCESS);
        order.setTotalPrice(totalPrice);
        orderDao.updateById(order);
        log.info("订单已成功下单");
        log.info("=============ORDER END=================");
    }
}