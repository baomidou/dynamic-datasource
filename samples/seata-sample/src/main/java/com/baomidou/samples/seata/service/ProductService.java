package com.baomidou.samples.seata.service;

public interface ProductService {

    /**
     * 扣减库存
     *
     * @param productId 商品 ID
     * @param amount    扣减数量
     * @return 商品总价
     */
    Double reduceStock(Long productId, Integer amount);
}