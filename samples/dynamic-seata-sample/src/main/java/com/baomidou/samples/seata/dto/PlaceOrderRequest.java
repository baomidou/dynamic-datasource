package com.baomidou.samples.seata.dto;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequest {

  @NotNull
  private Long userId;

  @NotNull
  private Long productId;

  @NotNull
  private Integer amount;
}