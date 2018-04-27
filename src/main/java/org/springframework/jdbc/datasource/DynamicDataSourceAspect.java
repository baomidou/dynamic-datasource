package org.springframework.jdbc.datasource;

import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

@Aspect
@Order(-10)
@Slf4j
public class DynamicDataSourceAspect {

  private DynamicRoutingDataSource dynamicRoutingDataSource;

  public DynamicDataSourceAspect(DynamicRoutingDataSource dynamicRoutingDataSource) {
    this.dynamicRoutingDataSource = dynamicRoutingDataSource;
  }

  @Before("@annotation(org.springframework.jdbc.datasource.DynamicDataSource)")
  public void chooseDataSource(JoinPoint point) {
    DynamicDataSource dynamicDataSource = ((MethodSignature) point.getSignature()).getMethod()
        .getAnnotation(DynamicDataSource.class);
    DynamicDataSourceContextHolder.setDataSourceLookupKey(dynamicDataSource.value());
  }

  @After("@annotation(org.springframework.jdbc.datasource.DynamicDataSource)")
  public void clearDataSource(JoinPoint point) {
    DynamicDataSourceContextHolder.clearDataSourceLookupKey();
  }

}