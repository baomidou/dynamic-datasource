package com.baomidou.samples.spel;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.baomidou.samples.spel.mapper")
public class SpelApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpelApplication.class, args);
    }

}