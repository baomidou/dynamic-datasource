package com.baomidou.samples.nest.service;

import com.baomidou.samples.nest.entiry.Teacher;

import java.util.List;


public interface TeacherService {

    boolean addTeacherWithTx(String name, Integer age);

    boolean addTeacherNoTx(String name, Integer age);


    List<Teacher> selectTeachers();

    void selectTeachersInnerStudents();
}
