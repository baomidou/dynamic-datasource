package com.baomidou.samples.dynamicload.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author TaoYu
 * @date 2020/1/27
 */
@Data
public class DataSourceDTO {

    @NotBlank
    @ApiModelProperty(value = "连接池名称", example = "test")
    private String pollName;

    @NotBlank
    @ApiModelProperty(value = "JDBC driver", example = "org.h2.Driver")
    private String driverClassName;

    @NotBlank
    @ApiModelProperty(value = "JDBC url 地址", example = "jdbc:h2:mem:test10")
    private String url;

    @NotBlank
    @ApiModelProperty(value = "JDBC 用户名", example = "sa")
    private String username;

    @ApiModelProperty(value = "JDBC 密码")
    private String password;

}
