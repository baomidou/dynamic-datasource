package com.baomidou.dynamic.datasource.fixture.v1.service.spel;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

import java.nio.charset.StandardCharsets;

public class CustomResultHandler implements ResultHandler {
    @Override
    public void handle(MvcResult result) {
        result.getResponse().setCharacterEncoding(StandardCharsets.UTF_8.name());
    }
}
