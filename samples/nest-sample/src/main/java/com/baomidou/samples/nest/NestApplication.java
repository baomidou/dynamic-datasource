package com.baomidou.samples.nest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.baomidou.samples.nest.mapper")
public class NestApplication {

    public static void main(String[] args) {
        SpringApplication.run(NestApplication.class, args);
    }

}