package com.baomidou.samples.nest.test;


import com.baomidou.samples.nest.Application;
import com.baomidou.samples.nest.service.SchoolService;
import com.baomidou.samples.nest.service.StudentService;
import com.baomidou.samples.nest.service.TeacherService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)

public class ApplicationTest {

    private Random random = new Random();

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private SchoolService schoolService;

    @Autowired
    private DataSource dataSource;

    @Before
    public void beforeTest() {
        try {
            Connection connection = dataSource.getConnection();
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS  teacher (\n" +
                    "  id BIGINT(20) NOT NULL AUTO_INCREMENT,\n" +
                    "  name VARCHAR(30) NULL DEFAULT NULL ,\n" +
                    "  age INT(11) NULL DEFAULT NULL ,\n" +
                    "  PRIMARY KEY (id)\n" +
                    ");");
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS  student (\n" +
                    "  id BIGINT(20) NOT NULL AUTO_INCREMENT,\n" +
                    "  name VARCHAR(30) NULL DEFAULT NULL ,\n" +
                    "  age INT(11) NULL DEFAULT NULL ,\n" +
                    "  PRIMARY KEY (id)\n" +
                    ");");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


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