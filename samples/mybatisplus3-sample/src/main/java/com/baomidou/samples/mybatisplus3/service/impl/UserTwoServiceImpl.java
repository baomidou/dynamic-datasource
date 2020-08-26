package com.baomidou.samples.mybatisplus3.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.samples.mybatisplus3.entity.User;
import com.baomidou.samples.mybatisplus3.mapper.UserMapper;
import com.baomidou.samples.mybatisplus3.service.UserService;
import com.baomidou.samples.mybatisplus3.service.UserTwoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: zs
 * @Date: 2020/8/26 14:28
 */
@Service
public class UserTwoServiceImpl extends ServiceImpl<UserMapper, User> implements UserTwoService {

    @Override
    @DS("slave_1")
    @Transactional(rollbackFor = Exception.class)
    public void insert() {
        User user=new User();
        user.setName("从");
        user.setAge(33);

        baseMapper.insert(user);
    }
}
