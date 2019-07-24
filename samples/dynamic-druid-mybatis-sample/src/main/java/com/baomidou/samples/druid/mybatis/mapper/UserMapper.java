package com.baomidou.samples.druid.mybatis.mapper;

import com.baomidou.samples.druid.mybatis.entity.User;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper {

  @Insert("INSERT INTO user (name,age) values (#{name},#{age})")
  boolean addUser(@Param("name") String name, @Param("age") Integer age);

  @Select("SELECT * FROM user where age > #{age}")
  List<User> selectUsers(@Param("age") Integer age);
}
