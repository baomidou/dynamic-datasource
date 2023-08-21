/*
 * Copyright © 2018 organization baomidou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baomidou.dynamic.datasource.fixture;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.fixture.service.nest.*;
import org.h2.tools.Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;
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
    TeacherService teacherService;

    @Autowired
    StudentService studentService;

    @Autowired
    SchoolService schoolService;

    @Test
    void testNest() {
        DataSourceProperty masterDataSourceProperty = new DataSourceProperty();
        masterDataSourceProperty.setPoolName("master");
        masterDataSourceProperty.setDriverClassName("org.h2.Driver");
        masterDataSourceProperty.setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE;INIT=RUNSCRIPT FROM 'classpath:db/add-remove-datasource.sql'");
        masterDataSourceProperty.setUsername("sa");
        masterDataSourceProperty.setPassword("");
        DataSourceProperty teacherDataSourceProperty = new DataSourceProperty();
        teacherDataSourceProperty.setPoolName("teacher");
        teacherDataSourceProperty.setDriverClassName("org.h2.Driver");
        teacherDataSourceProperty.setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE;INIT=RUNSCRIPT FROM 'classpath:db/add-remove-datasource.sql'");
        teacherDataSourceProperty.setUsername("sa");
        teacherDataSourceProperty.setPassword("");
        DataSourceProperty studentDataSourceProperty = new DataSourceProperty();
        studentDataSourceProperty.setPoolName("student");
        studentDataSourceProperty.setDriverClassName("org.h2.Driver");
        studentDataSourceProperty.setUrl("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_ON_EXIT=FALSE;INIT=RUNSCRIPT FROM 'classpath:db/add-remove-datasource.sql'");
        studentDataSourceProperty.setUsername("sa");
        studentDataSourceProperty.setPassword("");
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
