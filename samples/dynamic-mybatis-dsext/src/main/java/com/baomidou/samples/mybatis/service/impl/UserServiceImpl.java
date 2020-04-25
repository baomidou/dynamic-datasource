package com.baomidou.samples.mybatis.service.impl;


import com.baomidou.samples.annotation.MasterDS;
import com.baomidou.samples.annotation.SlaveDS;
import com.baomidou.samples.mybatis.entity.User;
import com.baomidou.samples.mybatis.mapper.UserMapper;
import com.baomidou.samples.mybatis.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 可以不指定 @MasterDS
     *
     * @param user
     */
    @MasterDS
    @Override
    public void addUser(User user) {
        userMapper.addUser(user.getName(), user.getAge());
    }

    /**
     * 指定 @SlaveDS
     *
     * @return
     */
    @SlaveDS
    @Override
    public List selectUsersFromDs() {
        return userMapper.selectUsers();
    }

    /**
     * 指定 @SlaveDS
     *
     * @return
     */
    @SlaveDS
    @Override
    public List selectUserFromDsGroup() {
        return userMapper.selectUsers();
    }
}
