package com.baomidou.samples.mybatisplus3.controller;


import com.baomidou.samples.mybatisplus3.entity.User;
import com.baomidou.samples.mybatisplus3.service.UserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/users")
@Api(tags = "用户列表")
public class UserController {

    private static final Random RANDOM = new Random();

    @Autowired
    private UserService userService;

    /**
     * 简单事务切换测试
     */
    @PostMapping("/test")
    public void test(){
        userService.test();
    }

    /**
     * service嵌套事务切换测试同一个事务管理器是否回滚
     *
     *
     *TODO 这里回滚有点问题
     *  DynamicRoutingDataSource和ItemDataSource冲突，导致事务管理器失效，
     *  运行了doRollback 但是数据没恢复
     *
     *
     */
    @PostMapping("/test2")
    public void test2(){
//        userService.test2();
    }

}
