package com.baomidou.samples.mybatis.service;


import com.baomidou.samples.mybatis.entity.User;

import java.util.List;

public interface UserService {

    List<User> selectUsersFromMaster();

    List<User> selectUsersFromSlave();

    void addUser(User user);

    void deleteUserById(Long id);
}
