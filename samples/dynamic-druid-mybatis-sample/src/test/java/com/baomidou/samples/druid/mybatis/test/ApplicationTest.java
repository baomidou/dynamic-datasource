package com.baomidou.samples.druid.mybatis.test;

import com.baomidou.samples.druid.mybatis.Application;
import com.baomidou.samples.druid.mybatis.entity.User;
import com.baomidou.samples.druid.mybatis.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTest {

    private Random random = new Random();

    @Autowired
    private UserService userService;

    @Autowired
    private DataSource dataSource;

    @Before
    public void beforeTest() {
        try {
            Connection connection = dataSource.getConnection();
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS  USER (\n" +
                    "  id BIGINT(20) NOT NULL AUTO_INCREMENT,\n" +
                    "  name VARCHAR(30) NULL DEFAULT NULL ,\n" +
                    "  age INT(11) NULL DEFAULT NULL ,\n" +
                    "  PRIMARY KEY (id)\n" +
                    ");");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


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