package com.baomidou.samples.mybatisplus2.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

@Data
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Integer age;
}
