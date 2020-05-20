package com.baomidou.samples.druid.mybatis.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.druid.mybatis.entity.User;
import com.baomidou.samples.druid.mybatis.mapper.UserMapper;
import com.baomidou.samples.druid.mybatis.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public void addUser(User user) {
        userMapper.addUser(user.getName(), user.getAge());
    }

    @DS("slave_1")
    @Override
    public List selectUsersFromDs() {
        return userMapper.selectUsers(1);
    }

    @DS("slave")
    @Override
    public List selectUserFromDsGroup() {
        return userMapper.selectUsers(1);
    }
}
