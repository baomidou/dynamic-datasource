package com.baomidou.samples.spel.test;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.baomidou.samples.spel.Application;
import com.baomidou.samples.spel.entity.User;
import com.baomidou.samples.spel.service.UserService;
import javax.servlet.http.HttpSession;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class ApplicationTest {

  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;
  @Autowired
  private UserService userService;
  @Autowired
  private HttpSession session;

  @Before
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
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
