package com.baomidou.dynamic.datasource.spring.boot.autoconfigure.dbcp;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

import static com.baomidou.dynamic.datasource.spring.boot.autoconfigure.druid.DruidConsts.TEST_WHILE_IDLE;

/**
 * jdbc 相关配置
 *  * @author liushang
 *  * @since 2020/1/27
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@Slf4j
public class DbcpConfig {
    private String url;
    private String driver;
    private String username;
    private String password;
    private Integer initialSize;
    private Integer maxIdle;
    private Integer minIdle;
    private Integer maxActive;
    private Boolean logAbandoned;
    private Boolean removeAbandoned;
    private Integer removeAbandonedTimeout;
    private Integer maxWait;

    private Boolean testOnBorrow;
    private Boolean testOnReturn;
    private Boolean testWhileIdle;
    private String validationQuery;
    private Integer validationQueryTimeout;
    private Integer timeBetweenEvictionRunsMillis;
    private Integer numTestsPerEvictionRun;

}
