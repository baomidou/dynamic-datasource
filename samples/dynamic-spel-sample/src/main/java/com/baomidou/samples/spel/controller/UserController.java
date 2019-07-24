package com.baomidou.samples.spel.controller;

import com.baomidou.samples.spel.entity.User;
import com.baomidou.samples.spel.service.UserService;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

  @Autowired
  private UserService userService;

  @GetMapping("/users")
  public List<User> usersFromSession(HttpServletRequest request) {
    request.getSession().setAttribute("tenantName", "tenant1");
    return userService.selectSpelBySession();
  }

  @GetMapping("/users/header")
  public String usersFromHeader() {
    userService.selectSpelByHeader();
    return "success";
  }

}
