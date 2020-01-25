package com.baomidou.samples.seata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account {

  @TableId(type = IdType.AUTO)
  private Long id;

  /**
   * 余额
   */
  private Double balance;

  private Date lastUpdateTime;
}