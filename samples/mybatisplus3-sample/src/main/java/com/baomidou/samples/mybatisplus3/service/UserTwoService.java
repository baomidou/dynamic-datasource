package com.baomidou.samples.mybatisplus3.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.samples.mybatisplus3.entity.User;

/**
 * @Author: zs
 * @Date: 2020/8/26 14:28
 */
public interface UserTwoService extends IService<User> {

    void insert();
}
