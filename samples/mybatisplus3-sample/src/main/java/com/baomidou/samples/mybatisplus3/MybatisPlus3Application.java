package com.baomidou.samples.mybatisplus3;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.baomidou.samples.mybatisplus3.mapper")
public class MybatisPlus3Application {

    public static void main(String[] args) {
        SpringApplication.run(MybatisPlus3Application.class, args);
    }

}