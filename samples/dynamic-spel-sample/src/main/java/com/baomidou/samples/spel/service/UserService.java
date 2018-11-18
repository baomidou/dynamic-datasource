package com.baomidou.samples.spel.service;


import com.baomidou.samples.spel.entity.User;

import java.util.List;

public interface UserService {

    List selectSpelBySession();

    List selectSpelByHeader();

    List selectSpelByKey(String tenantName);

    List selecSpelByTenant(User user);
}
