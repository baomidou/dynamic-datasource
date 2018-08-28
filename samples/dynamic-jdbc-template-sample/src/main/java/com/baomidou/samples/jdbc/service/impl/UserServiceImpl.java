package com.baomidou.samples.jdbc.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.jdbc.entity.User;
import com.baomidou.samples.jdbc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@DS("slave")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DS("master") //默认是master可以省略这个注解,如果在类上注解了其他库，则@DS("master")不能省略
    @Override
    public void addUser(User user) {
        jdbcTemplate.update("INSERT INTO user (name,age) VALUES(?, ?)", new Object[]{user.getName(), user.getAge()});
    }

    @DS("slave_1")
    @Override
    public List selectUser1() {
        return jdbcTemplate.queryForList("SELECT * FROM user");
    }

    @DS("slave_2")
    @Override
    public List selectUser2() {
        return jdbcTemplate.queryForList("SELECT * FROM user");
    }

    //这个slave随机库
    @Override
    public List selectUser3() {
        return jdbcTemplate.queryForList("SELECT * FROM user");
    }

}
