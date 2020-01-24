package com.baomidou.samples.seata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.seata.dao.AccountDao;
import com.baomidou.samples.seata.entity.Account;
import com.baomidou.samples.seata.service.AccountService;
import io.seata.core.context.RootContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author HelloWoodes
 */
@Service
@AllArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

  private final AccountDao accountDao;

  /**
   * 事务传播特性设置为 REQUIRES_NEW 开启新的事务
   */
  @DS("account")
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public boolean reduceBalance(Long userId, Integer price) {
    log.info("=============PAY=================");
    log.info("当前 XID: {}", RootContext.getXID());

    log.info("检查用户 {} 余额", userId);
    Account account = accountDao.selectById(userId);

    Integer balance = account.getBalance();

    if (balance < price) {
      log.warn("用户 {} 余额不足，当前余额:{}", userId, balance);
      throw new RuntimeException("余额不足");
    }
    log.info("开始扣减用户 {} 余额", userId);

    account.setBalance(account.getBalance() - price);
    Integer record = accountDao.updateById(account);
    log.info("扣减用户 {} 余额结果:{}", userId, record > 0 ? "操作成功" : "扣减余额失败");
    return record > 0;
  }

}