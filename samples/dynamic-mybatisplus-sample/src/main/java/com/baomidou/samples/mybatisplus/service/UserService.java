package com.baomidou.samples.mybatisplus.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.samples.mybatisplus.entity.User;

public interface UserService extends IService<User> {

    void addUser(User user);

}
