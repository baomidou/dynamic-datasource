package com.baomidou.samples.spel.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.spel.entity.User;
import com.baomidou.samples.spel.mapper.UserMapper;
import com.baomidou.samples.spel.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@DS("slave")
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    @DS("#session.tenantName")
    public List<User> selectSpelBySession() {
        return userMapper.selectUsers();
    }

    @Override
    @DS("#header.tenantName")
    public List<User> selectSpelByHeader() {
        return userMapper.selectUsers();
    }

    @Override
    @DS("#tenantName")
    public List<User> selectSpelByKey(String tenantName) {
        return userMapper.selectUsers();
    }

    @Override
    @DS("#user.tenantName")
    public List<User> selecSpelByTenant(User user) {
        return userMapper.selectUsers();
    }
}
