package com.baomidou.samples.seata.service;

import com.baomidou.samples.seata.common.OperationResponse;
import com.baomidou.samples.seata.dto.PlaceOrderRequest;

public interface OrderService {

  /**
   * 下单
   *
   * @param placeOrderRequest 请求参数
   * @return 下单结果
   */
  OperationResponse placeOrder(PlaceOrderRequest placeOrderRequest);
}