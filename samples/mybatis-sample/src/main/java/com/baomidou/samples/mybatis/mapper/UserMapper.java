package com.baomidou.samples.mybatis.mapper;

import com.baomidou.samples.mybatis.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {

    @Insert("INSERT INTO user (name,age) values (#{name},#{age})")
    boolean addUser(@Param("name") String name, @Param("age") Integer age);

    @Select("SELECT * FROM user")
    List<User> selectUsers();

    @Delete("DELETE from user where id = #{id}")
    void deleteUserById(Long id);
}
