package com.baomidou.samples.seata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.baomidou.samples.seata.dao")
public class SeataApplication {

  public static void main(String[] args) {
    SpringApplication.run(SeataApplication.class, args);
  }

}