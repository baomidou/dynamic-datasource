package com.baomidou.samples.nest.service.impl;

import com.baomidou.samples.nest.service.SchoolService;
import com.baomidou.samples.nest.service.StudentService;
import com.baomidou.samples.nest.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolServiceImpl implements SchoolService {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Override
    public void selectTeachersAndStudents() {
        teacherService.selectTeachers();
        studentService.selectStudents();
    }

    @Override
    public void selectTeachersInnerStudents() {
        teacherService.selectTeachersInnerStudents();
    }

    @Override
    public void addTeacherAndStudent() {
        teacherService.addTeacherWithTx("ss", 1);
        studentService.addStudentWithTx("tt", 2);
    }

    @Override
    @Transactional
    public void addTeacherAndStudentWithTx() {
        teacherService.addTeacherWithTx("ss", 1);
        studentService.addStudentWithTx("tt", 2);
    }
}
