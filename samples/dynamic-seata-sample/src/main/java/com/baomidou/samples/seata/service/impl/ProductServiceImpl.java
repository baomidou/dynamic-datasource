package com.baomidou.samples.seata.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.seata.dao.ProductDao;
import com.baomidou.samples.seata.entity.Product;
import com.baomidou.samples.seata.service.ProductService;
import io.seata.core.context.RootContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {

  private final ProductDao productDao;

  /**
   * 事务传播特性设置为 REQUIRES_NEW 开启新的事务
   */
  @DS("product")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  public boolean reduceStock(Long productId, Integer amount) {
    log.info("=============STORAGE=================");
    log.info("当前 XID: {}", RootContext.getXID());

    // 检查库存
    log.info("检查 {} 库存", productId);
    Product product = productDao.selectById(productId);

    if (product.getStock() < amount) {
      log.warn("{} 库存不足，当前库存:{}", productId, product.getStock());
      throw new RuntimeException("库存不足");
    }

    log.info("开始扣减 {} 库存", productId);
    // 扣减库存
    product.setStock(product.getStock() - amount);
    Integer record = productDao.updateById(product);
    log.info("扣减 {} 库存结果:{}", productId, record > 0 ? "操作成功" : "扣减库存失败");
    return record > 0;

  }
}