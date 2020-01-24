package com.baomidou.samples.seata.entity;

import com.baomidou.samples.seata.common.OrderStatus;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Orders {

  private Integer id;

  /**
   * 用户ID
   */
  private Long userId;
  /**
   * 商品ID
   */
  private Long productId;
  /**
   * 订单状态
   */
  private OrderStatus status;
  /**
   *
   */
  private Integer payAmount;
}