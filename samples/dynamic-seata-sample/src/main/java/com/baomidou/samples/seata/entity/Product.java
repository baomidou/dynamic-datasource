package com.baomidou.samples.seata.entity;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Product {

  private Integer id;

  private Double price;

  private Integer stock;

  private Date lastUpdateTime;
}