package com.baomidou.samples.seata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.seata.dao.AccountDao;
import com.baomidou.samples.seata.entity.Account;
import com.baomidou.samples.seata.service.AccountService;
import io.seata.core.context.RootContext;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

  @Resource
  private AccountDao accountDao;

  /**
   * 事务传播特性设置为 REQUIRES_NEW 开启新的事务
   */
  @DS("account")
  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void reduceBalance(Long userId, Double price) {
    log.info("=============ACCOUNT START=================");
    log.info("当前 XID: {}", RootContext.getXID());

    Account account = accountDao.selectById(userId);
    Double balance = account.getBalance();
    log.info("下单用户{}余额为 {},商品总价为{}", userId, balance, price);

    if (balance < price) {
      log.warn("用户 {} 余额不足，当前余额:{}", userId, balance);
      throw new RuntimeException("余额不足");
    }
    log.info("开始扣减用户 {} 余额", userId);
    double currentBalance = account.getBalance() - price;
    account.setBalance(currentBalance);
    accountDao.updateById(account);
    log.info("扣减用户 {} 余额成功,扣减后用户账户余额为{}", userId, currentBalance);
    log.info("=============ACCOUNT END=================");
  }

}