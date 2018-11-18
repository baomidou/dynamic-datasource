package com.baomidou.samples.nest.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.nest.entiry.Student;
import com.baomidou.samples.nest.mapper.StudentMapper;
import com.baomidou.samples.nest.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@DS("student")
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentMapper studentMapper;

    @Override
    @Transactional
    public boolean addStudentWithTx(String name, Integer age) {
        return studentMapper.addStudent(name, age);
    }

    @Override
    public boolean addStudentNoTx(String name, Integer age) {
        return studentMapper.addStudent(name, age);
    }

    @Override
    public List<Student> selectStudents() {
        return studentMapper.selectStudents();
    }
}
