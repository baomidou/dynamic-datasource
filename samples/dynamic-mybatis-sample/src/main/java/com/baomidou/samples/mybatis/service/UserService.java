package com.baomidou.samples.jdbc.service;


import com.baomidou.samples.mybatis.entity.User;

import java.util.List;

public interface UserService {

    void addUser(User user);

    List selectUser1();

    List selectUser2();

    List selectUser3();
}
