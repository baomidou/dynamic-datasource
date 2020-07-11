package com.baomidou.samples.mybatisplus3.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.samples.mybatisplus3.entity.User;

@DS("slave")
public interface UserSlaveMapper extends BaseMapper<User> {
}
