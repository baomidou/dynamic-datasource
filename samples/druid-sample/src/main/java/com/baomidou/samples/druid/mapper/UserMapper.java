package com.baomidou.samples.druid.mapper;

import com.baomidou.samples.druid.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {

    @Insert("INSERT INTO user (name,age) values (#{name},#{age})")
    boolean addUser(String name, Integer age);

    @Select("SELECT * FROM user where age > #{age}")
    List<User> selectUsers(@Param("age") Integer age);

    @Delete("DELETE from user where id = #{id}")
    void deleteUserById(Long id);
}
