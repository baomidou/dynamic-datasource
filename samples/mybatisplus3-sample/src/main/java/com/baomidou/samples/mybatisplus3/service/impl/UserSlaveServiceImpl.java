package com.baomidou.samples.mybatisplus3.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.samples.mybatisplus3.entity.User;
import com.baomidou.samples.mybatisplus3.mapper.UserMapper;
import com.baomidou.samples.mybatisplus3.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@DS("slave")
@Service
public class UserSlaveServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

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
