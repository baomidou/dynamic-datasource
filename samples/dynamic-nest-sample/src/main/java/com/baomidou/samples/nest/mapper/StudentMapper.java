package com.baomidou.samples.nest.mapper;

import com.baomidou.samples.nest.entiry.Student;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StudentMapper {

    @Insert("INSERT INTO student (name,age) values (#{name},#{age})")
    boolean addStudent(@Param("name") String name, @Param("age") Integer age);

    @Select("SELECT * FROM student")
    List<Student> selectStudents();
}
