package com.baomidou.samples.mybatisplus3.test;

import com.baomidou.samples.mybatisplus.Application;
import com.baomidou.samples.mybatisplus.entity.User;
import com.baomidou.samples.mybatisplus.service.UserService;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTest {

  private Random random = new Random();

  @Autowired
  private UserService userService;

  @Test
  public void testAddUser() {
    User user = new User();
    user.setName("测试用户" + random.nextInt());
    user.setAge(random.nextInt(100));
    userService.addUser(user);
  }

  @Test
  public void testSelectUser() {
    userService.list(null);
  }
}
