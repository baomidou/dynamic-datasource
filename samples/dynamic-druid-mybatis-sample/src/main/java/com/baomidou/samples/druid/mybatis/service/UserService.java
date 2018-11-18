package com.baomidou.samples.druid.mybatis.service;


import com.baomidou.samples.druid.mybatis.entity.User;

import java.util.List;

public interface UserService {

    void addUser(User user);

    List selectUsersFromDs();

    List selectUserFromDsGroup();
}
