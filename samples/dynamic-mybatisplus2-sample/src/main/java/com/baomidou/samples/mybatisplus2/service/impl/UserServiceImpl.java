package com.baomidou.samples.mybatisplus2.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.samples.mybatisplus2.entity.User;
import com.baomidou.samples.mybatisplus2.mapper.UserMapper;
import com.baomidou.samples.mybatisplus2.service.UserService;
import org.springframework.stereotype.Service;

@Service
@DS("slave")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    @DS("master")//这里必须包一层，不能调用mp默认的插入，因为会走到从库去
    public void addUser(User user) {
        baseMapper.insert(user);
    }
}
