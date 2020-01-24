package com.baomidou.samples.seata.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationResponse {

  private boolean success;

  private String message;

  private Object data;
}