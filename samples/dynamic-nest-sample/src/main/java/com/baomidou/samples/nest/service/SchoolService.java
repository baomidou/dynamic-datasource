package com.baomidou.samples.nest.service;

public interface SchoolService {

    void selectTeachersAndStudents();

    void selectTeachersInnerStudents();

    void addTeacherAndStudent();

    void addTeacherAndStudentWithTx();
}
