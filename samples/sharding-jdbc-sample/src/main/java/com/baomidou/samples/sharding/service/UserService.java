package com.baomidou.samples.sharding.service;


import com.baomidou.samples.sharding.entity.User;

import java.util.List;

public interface UserService {


    List<User> selectUsersFromMaster();

    List<User> selectUsersFromShardingSlave();

    void addUser(User user);

    void deleteUserById(Long id);
}
