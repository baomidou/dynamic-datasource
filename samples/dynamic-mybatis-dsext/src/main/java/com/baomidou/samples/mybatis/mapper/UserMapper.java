package com.baomidou.samples.mybatis.mapper;

import com.baomidou.samples.mybatis.entity.User;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

//@DS("slave")这里可以使用但不建议，不要和service同时使用
public interface UserMapper {

  @Insert("INSERT INTO user (name,age) values (#{name},#{age})")
  boolean addUser(@Param("name") String name, @Param("age") Integer age);

  @Select("SELECT * FROM user")
  List<User> selectUsers();
}
