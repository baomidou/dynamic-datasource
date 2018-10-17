package com.baomidou.samples.mybatis.test;


import com.baomidou.samples.spel.Application;
import com.baomidou.samples.spel.entity.User;
import com.baomidou.samples.spel.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;

    @Test
    public void testselectSpelBySession() {
        session.setAttribute("tenantName","tenant1");
        userService.selectSpelBySession();
    }

    @Test
    public void testselectSpelByHeader() {
        //fixme 这里模拟不了添加请求头啊
        userService.selectSpelByHeader();
    }

    @Test
    public void testselectSpelByKey() {
        userService.selectSpelByKey("tenant1");
    }

    @Test
    public void selecSpelByTenant() {
        User user = new User();
        user.setTenantName("tenant2");
        userService.selecSpelByTenant(user);
    }

}
