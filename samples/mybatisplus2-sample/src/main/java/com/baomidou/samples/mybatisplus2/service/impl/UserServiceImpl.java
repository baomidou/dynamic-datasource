package com.baomidou.samples.mybatisplus2.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.samples.mybatisplus2.entity.User;
import com.baomidou.samples.mybatisplus2.mapper.UserMapper;
import com.baomidou.samples.mybatisplus2.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @DS("slave")
    @Override
    public List<User> selectUsersFromSlave() {
        return baseMapper.selectList(null);
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
