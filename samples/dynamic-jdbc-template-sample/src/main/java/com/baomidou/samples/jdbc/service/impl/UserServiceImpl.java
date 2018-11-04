package com.baomidou.samples.jdbc.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.jdbc.entity.User;
import com.baomidou.samples.jdbc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void addUser(User user) {
        jdbcTemplate.update("INSERT INTO user (name,age) VALUES(?, ?)", new Object[]{user.getName(), user.getAge()});
    }

    @DS("slave_1")
    @Override
    public List selectUsersFromDs() {
        return jdbcTemplate.queryForList("SELECT * FROM user");
    }

    @DS("slave")
    @Override
    public List selectUserFromDsGroup() {
        return jdbcTemplate.queryForList("SELECT * FROM user");
    }
}
