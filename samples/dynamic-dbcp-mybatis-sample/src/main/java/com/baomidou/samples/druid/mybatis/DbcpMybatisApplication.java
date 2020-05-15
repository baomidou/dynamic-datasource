package com.baomidou.samples.druid.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@MapperScan("com.baomidou.samples.druid.mybatis.mapper")
public class DbcpMybatisApplication {

    public static void main(String[] args) {
        SpringApplication.run(DbcpMybatisApplication.class, args);
    }

}