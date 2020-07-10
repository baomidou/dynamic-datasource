package com.baomidou.samples.nest.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.nest.entiry.Teacher;
import com.baomidou.samples.nest.mapper.TeacherMapper;
import com.baomidou.samples.nest.service.StudentService;
import com.baomidou.samples.nest.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@DS("teacher")
public class TeacherServiceImpl implements TeacherService {

    @Resource
    private TeacherMapper teacherMapper;
    @Autowired
    private StudentService studentService;

    @Override
    @Transactional
    public boolean addTeacherWithTx(String name, Integer age) {
        return teacherMapper.addTeacher(name, age);
    }

    @Override
    public boolean addTeacherNoTx(String name, Integer age) {
        return teacherMapper.addTeacher(name, age);
    }

    @Override
    public List<Teacher> selectTeachers() {
        return teacherMapper.selectTeachers();
    }

    @Override
    public void selectTeachersInnerStudents() {
        teacherMapper.selectTeachers();
        studentService.selectStudents();
    }
}
