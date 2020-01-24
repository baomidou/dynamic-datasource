package com.baomidou.samples.seata.service;

public interface AccountService {

  /**
   * @param userId 用户 ID
   * @param price 扣减金额
   */
  boolean reduceBalance(Long userId, Integer price);

}