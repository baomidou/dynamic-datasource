package com.baomidou.samples.nest.service;

import com.baomidou.samples.nest.entiry.Student;

import java.util.List;

public interface StudentService {

    boolean addStudentWithTx(String name, Integer age);

    boolean addStudentNoTx(String name, Integer age);


    List<Student> selectStudents();
}
