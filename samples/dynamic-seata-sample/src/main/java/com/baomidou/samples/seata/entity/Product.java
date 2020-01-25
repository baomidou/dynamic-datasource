package com.baomidou.samples.seata.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Product {

  @TableId(type = IdType.AUTO)
  private Integer id;
  /**
   * 价格
   */
  private Double price;
  /**
   * 库存
   */
  private Integer stock;

  private Date lastUpdateTime;
}