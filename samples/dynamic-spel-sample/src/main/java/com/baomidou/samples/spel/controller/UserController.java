package com.baomidou.samples.spel.controller;

import com.baomidou.samples.spel.entity.User;
import com.baomidou.samples.spel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public List<User> usersFromSession(HttpServletRequest request) {
        request.getSession().setAttribute("tenantName", "tenant1");
        return userService.selectSpelBySession();
    }

//    @Autowired
//    private Kaptcha kaptcha;
//
//    @GetMapping("/render")
//    public void render() {
//        kaptcha.render();
//    }
}
