package com.baomidou.samples.seata.entity;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account {

  private Long id;

  /**
   * 余额
   */
  private Integer balance;

  private Date lastUpdateTime;
}