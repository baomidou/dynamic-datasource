package com.baomidou.samples.jdbc.service;

import com.baomidou.samples.jdbc.entity.User;

import java.util.List;

public interface UserService {

    List<User> selectUsersFromMaster();

    List<User> selectUsersFromSlave();

    void addUser(User user);

    void deleteUserById(Long id);
}
