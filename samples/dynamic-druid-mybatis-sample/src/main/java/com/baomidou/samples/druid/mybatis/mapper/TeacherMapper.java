package com.baomidou.samples.druid.mybatis.mapper;

import com.baomidou.samples.druid.mybatis.entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TeacherMapper {


    @Select("SELECT * FROM teacher")
    List<User> selectTeachers();
}
