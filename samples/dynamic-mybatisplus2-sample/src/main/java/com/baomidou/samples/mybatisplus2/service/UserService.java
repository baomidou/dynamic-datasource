package com.baomidou.samples.mybatisplus2.service;


import com.baomidou.mybatisplus.service.IService;
import com.baomidou.samples.mybatisplus2.entity.User;

public interface UserService extends IService<User> {

    void addUser(User user);

}
