package com.baomidou.samples.seata.service;

public interface ProductService {

  /**
   * 扣减库存
   *
   * @param productId 商品 ID
   * @param amount 扣减数量
   * @return 操作结果
   */
  boolean reduceStock(Long productId, Integer amount);
}