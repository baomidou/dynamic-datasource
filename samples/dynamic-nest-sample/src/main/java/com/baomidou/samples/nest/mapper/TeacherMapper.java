package com.baomidou.samples.nest.mapper;

import com.baomidou.samples.nest.entiry.Teacher;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TeacherMapper {

    @Insert("INSERT INTO teacher (name,age) values (#{name},#{age})")
    boolean addTeacher(@Param("name") String name, @Param("age") Integer age);

    @Select("SELECT * FROM teacher")
    List<Teacher> selectTeachers();
}
