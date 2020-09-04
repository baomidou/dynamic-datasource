package com.baomidou.samples.transaction.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author Hccake 2020/9/3
 * @version 1.0
 */
@Data
@TableName("c")
public class C {

    @TableId
    private Integer id;

    private String name;
}
