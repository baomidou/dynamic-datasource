package com.baomidou.samples.jdbc.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.jdbc.entity.User;
import com.baomidou.samples.jdbc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final RowMapper<User> ROW_MAPPER = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setAge(rs.getInt("age"));
            return user;
        }
    };
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<User> selectUsersFromMaster() {
        return jdbcTemplate.query("SELECT * FROM user", ROW_MAPPER);
    }

    @DS("slave")
    @Override
    public List<User> selectUsersFromSlave() {
        return jdbcTemplate.query("SELECT * FROM user", ROW_MAPPER);
    }

    @Override
    public void addUser(User user) {
        jdbcTemplate.update("INSERT INTO user (name,age) VALUES(?, ?)",
                new Object[]{user.getName(), user.getAge()});
    }

    @Override
    public void deleteUserById(Long id) {
        jdbcTemplate.update("DELETE FROM user where  id =" + id);
    }
}
