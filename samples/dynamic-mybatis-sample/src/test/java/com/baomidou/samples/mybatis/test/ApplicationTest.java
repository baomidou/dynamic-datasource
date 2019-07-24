package com.baomidou.samples.mybatis.test;


import com.baomidou.samples.mybatis.Application;
import com.baomidou.samples.mybatis.entity.User;
import com.baomidou.samples.mybatis.service.UserService;
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
  public void addUser() {
    User user = new User();
    user.setName("测试用户" + random.nextInt());
    user.setAge(random.nextInt(100));
    userService.addUser(user);
  }

  @Test
  public void selectUsersFromDs() {
    userService.selectUsersFromDs();
  }

  @Test
  public void selectUserFromDsGroup() {
    userService.selectUserFromDsGroup();
  }
}
