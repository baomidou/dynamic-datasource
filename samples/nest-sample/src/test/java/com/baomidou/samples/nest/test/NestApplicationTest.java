package com.baomidou.samples.nest.test;


import com.baomidou.samples.nest.NestApplication;
import com.baomidou.samples.nest.service.SchoolService;
import com.baomidou.samples.nest.service.StudentService;
import com.baomidou.samples.nest.service.TeacherService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NestApplication.class)

public class NestApplicationTest {

    private Random random = new Random();

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private SchoolService schoolService;

    @Test
    public void nest1() {
        //直接在controller
        teacherService.selectTeachers();
        studentService.selectStudents();
    }

    @Test
    public void nest2() {
        schoolService.selectTeachersAndStudents();
    }

    @Test
    public void nest3() {
        schoolService.selectTeachersInnerStudents();
    }

    @Test
    public void tx() {
        teacherService.selectTeachers();
    }
}