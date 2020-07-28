package com.baomidou.samples.mybatisplus3.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.samples.mybatisplus3.entity.User;
import com.baomidou.samples.mybatisplus3.mapper.UserMapper;
import com.baomidou.samples.mybatisplus3.mapper.UserSlaveMapper;
import com.baomidou.samples.mybatisplus3.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserSlaveMapper userSlaveMapper;


    @DS("slave")
    @Override
    public List<User> selectUsersFromSlave() {
        return baseMapper.selectList(null);
    }

    /**
     * mapper 是嵌套代理.
     * jdkProxyA->jdkProxyB->InvocationHandler
     * 3.1.1以及之前的代码在mapper上使用注解可能会出错。 在某些特殊环境下mapper会被多层代理。
     * 所以还是建议大家在service上注解，当然从3.1.2开始会解决这一问题。
     */
    @Override
    public List<User> specialSelectUsersFromSlave() {
        return userSlaveMapper.selectList(null);
    }

    @Override
    public void addUser(User user) {
        baseMapper.insert(user);
    }

    @Override
    public void deleteUserById(Long id) {
        baseMapper.deleteById(id);
    }
}
