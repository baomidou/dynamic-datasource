package org.springframework.boot.autoconfigure.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Aspect
@Component
@Order(-10)
@Slf4j
public class DynamicDataSourceAspect {

  @Before("@annotation(targetDataSource)")
  public void chooseDataSource(JoinPoint point, TargetDataSource targetDataSource) {
    String dsId = targetDataSource.value();
    if (StringUtils.isEmpty(dsId) || !DynamicDataSourceContextHolder.containDataSource(dsId)) {
      log.warn("数据源[{}]不存在，使用主数据源 > {}", dsId, point.getSignature());
    } else {
      log.debug("切换数据源 : {} > {}", dsId, point.getSignature());
      DynamicDataSourceContextHolder.setDataSource(dsId);
    }
  }

  @After("@annotation(targetDataSource)")
  public void clearDataSource(JoinPoint point, TargetDataSource targetDataSource) {
    DynamicDataSourceContextHolder.clearDataSource();
  }

}