package com.baomidou.samples.sharding.controller;


import com.baomidou.samples.sharding.entity.User;
import com.baomidou.samples.sharding.service.UserService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
@Api(tags = "用户列表")
public class UserController {

    private static final Random RANDOM = new Random();
    private final UserService userService;

    /**
     * 动态数据源的主库
     * @return
     */
    @GetMapping("master")
    public List<User> master() {
        return userService.selectUsersFromMaster();
    }



    /**
     * 动态数据源代理的sharding-jdbc的从库,经过两次选择
     * 第一次:  dynamic-ds => sharding-ds
     * 第二次:  sharding-ds => slave
     * @return
     */
    @GetMapping("sharding")
    public List<User> shardingSlave() {
        return userService.selectUsersFromShardingSlave();
    }

    /**
     * 动态数据源代理的sharding-jdbc的主库,经过两次选择
     * 第一次:  dynamic-ds => sharding-ds
     * 第二次:  sharding-ds => master
     * @return
     */
    @PostMapping("sharding")
    public User addUser() {
        User user = new User();
        user.setName("测试用户" + RANDOM.nextInt());
        user.setAge(RANDOM.nextInt(100));
        userService.addUser(user);
        return user;
    }

    /**
     * 动态数据源代理的sharding-jdbc的主库,经过两次选择
     * 第一次:  dynamic-ds => sharding-ds
     * 第二次:  sharding-ds => master
     * @return
     */
    @DeleteMapping("sharding/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "成功删除用户" + id;
    }
}
