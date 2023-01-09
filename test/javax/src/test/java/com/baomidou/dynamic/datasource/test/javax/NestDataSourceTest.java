package com.baomidou.dynamic.datasource.test.javax;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.test.javax.service.nest.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = NestApplication.class, webEnvironment = RANDOM_PORT)
public class NestDataSourceTest {
    @Autowired
    DataSource dataSource;
    @Autowired
    DefaultDataSourceCreator dataSourceCreator;
    @Autowired
    private TeacherService teacherService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private SchoolService schoolService;

    @Test
    void testNest() {
        DataSourceProperty masterDataSourceProperty = new DataSourceProperty()
                .setPoolName("master").setDriverClassName("org.h2.Driver")
                .setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE;INIT=RUNSCRIPT FROM 'classpath:db/add-remove-datasource.sql'")
                .setUsername("sa").setPassword("");
        DataSourceProperty teacherDataSourceProperty = new DataSourceProperty()
                .setPoolName("teacher").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DataSourceProperty studentDataSourceProperty = new DataSourceProperty()
                .setPoolName("student").setDriverClassName("org.h2.Driver").setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE")
                .setUsername("sa").setPassword("");
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        ds.addDataSource(masterDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(masterDataSourceProperty));
        ds.addDataSource(teacherDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(teacherDataSourceProperty));
        ds.addDataSource(studentDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(studentDataSourceProperty));
        assertThat(ds.getDataSources().keySet()).contains("master", "teacher", "student");
        assertThat(teacherService.addTeacherWithTx("ss", 1)).isEqualTo(1);
        assertThat(studentService.addStudentWithTx("tt", 2)).isEqualTo(1);
        assertThat(teacherService.selectTeachers()).isEqualTo(List.of(new Teacher(1, "tt", 2)));
        assertThat(studentService.selectStudents()).isEqualTo(List.of(new Student(1, "tt", 2)));
        assertThat(schoolService.addTeacherAndStudentWithTx()).isEqualTo(2);
        assertThat(teacherService.selectTeachers()).isEqualTo(List.of(new Teacher(1, "tt", 2), new Teacher(2, "bb", 4)));
        assertThat(studentService.selectStudents()).isEqualTo(List.of(new Student(1, "tt", 2), new Student(2, "bb", 4)));
    }
}

@SpringBootApplication
class NestApplication {
    public static void main(String[] args) {
        SpringApplication.run(NestApplication.class, args);
    }
}
