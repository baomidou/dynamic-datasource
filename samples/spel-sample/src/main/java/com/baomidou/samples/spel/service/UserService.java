package com.baomidou.samples.spel.service;


import com.baomidou.samples.spel.entity.User;

import java.util.List;

public interface UserService {

    List<User> selectSpelBySession();

    List<User> selectSpelByHeader();

    List<User> selectSpelByKey(String tenantName);

    List<User> selecSpelByTenant(User user);
}
