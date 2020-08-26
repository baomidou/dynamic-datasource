package com.baomidou.samples.mybatisplus3.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.samples.mybatisplus3.entity.User;
import com.baomidou.samples.mybatisplus3.mapper.UserMapper;
import com.baomidou.samples.mybatisplus3.service.UserService;
import com.baomidou.samples.mybatisplus3.service.UserTwoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    @Autowired
    private UserTwoService userTwoService;

    @Override
    @DS("master")
    @Transactional(rollbackFor = Exception.class)
    public void test() {
        User user=new User();
        user.setName("主");
        user.setAge(22);

        baseMapper.insert(user);

        userTwoService.insert();
    }

    @Override
    @DS("slave_1")
    @Transactional(rollbackFor = Exception.class)
    public void test2() {
        User user=new User();
        user.setName("主");
        user.setAge(22);

        baseMapper.insert(user);
        int i=1/0;
//        userSlaveService.insert2();

    }

}
