package org.springframework.jdbc.datasource;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

@Aspect
@Order(-10)
@Slf4j
public class DynamicDataSourceAspect {

  @Before("@annotation(org.springframework.jdbc.datasource.DynamicDataSource)")
  public void chooseDataSource(JoinPoint point) {
    DynamicDataSource dynamicDataSource = ((MethodSignature) point.getSignature()).getMethod()
        .getAnnotation(DynamicDataSource.class);
    String dsId = dynamicDataSource.value();
    if (StringUtils.isEmpty(dsId) || !DynamicDataSourceContextHolder.containDataSource(dsId)) {
      log.warn("datasource [{}] is not present,use default master > {}", dsId, point.getSignature());
    } else {
      log.debug("switch datasource to {} > {}", dsId, point.getSignature());
      DynamicDataSourceContextHolder.setDataSource(dsId);
    }
  }

  @After("@annotation(org.springframework.jdbc.datasource.DynamicDataSource)")
  public void clearDataSource(JoinPoint point) {
    DynamicDataSourceContextHolder.clearDataSource();
  }

}