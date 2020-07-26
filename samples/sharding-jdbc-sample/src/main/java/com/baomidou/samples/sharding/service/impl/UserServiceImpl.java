package com.baomidou.samples.sharding.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.sharding.entity.User;
import com.baomidou.samples.sharding.mapper.UserMapper;
import com.baomidou.samples.sharding.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;


    @DS("master")
    @Override
    public List<User> selectUsersFromMaster() {
        return userMapper.selectUsers();
    }


    @Override
    @DS("sharding")
    public List<User> selectUsersFromShardingSlave() {
        return userMapper.selectUsers();
    }

    @DS("sharding")
    @Override
    public void addUser(User user) {
        userMapper.addUser(user.getName(), user.getAge());
    }

    @DS("sharding")
    @Override
    public void deleteUserById(Long id) {
        userMapper.deleteUserById(id);
    }
}
