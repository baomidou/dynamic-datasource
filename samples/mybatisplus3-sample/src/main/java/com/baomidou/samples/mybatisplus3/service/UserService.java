package com.baomidou.samples.mybatisplus3.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.samples.mybatisplus3.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    List<User> selectUsers();

    void addUser(User user);

    void deleteUserById(Long id);

    List<User> selectSlaveUsers();
}
