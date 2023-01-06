package com.baomidou.dynamic.datasource.test.javax.service.spel;

public record User(Integer id, String name, Integer age, String tenantName) {
    public User(String tenantName) {
        this(null, null, null, tenantName);
    }
}
