package com.baomidou.samples.jdbc.service;

import com.baomidou.samples.jdbc.entity.User;

import java.util.List;

public interface UserService {

    void addUser(User user);

    List selectUsersFromDs();

    List selectUserFromDsGroup();
}
