package com.baomidou.samples.mybatisplus2.service;


import com.baomidou.mybatisplus.service.IService;
import com.baomidou.samples.mybatisplus2.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    List<User> selectUsersFromSlave();

    void addUser(User user);

    void deleteUserById(Long id);
}
