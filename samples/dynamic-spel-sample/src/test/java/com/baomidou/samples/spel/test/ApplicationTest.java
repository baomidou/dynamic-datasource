package com.baomidou.samples.spel.test;

import com.baomidou.samples.spel.Application;
import com.baomidou.samples.spel.entity.User;
import com.baomidou.samples.spel.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private DataSource dataSource;

    private MockMvc mockMvc;
    @Autowired
    private UserService userService;
    @Autowired
    private HttpSession session;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

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
    public void selectSpelBySession() {
        session.setAttribute("tenantName", "tenant1");
        userService.selectSpelBySession();
    }

    @Test
    public void selectSpelByHeader() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/header")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("tenantName", "tenant1")
        )
                .andDo(print()).andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
    }

    @Test
    public void selectSpelByKey() {
        userService.selectSpelByKey("tenant1");
    }

    @Test
    public void selecSpelByTenant() {
        User user = new User();
        user.setTenantName("tenant2");
        userService.selecSpelByTenant(user);
    }

}
