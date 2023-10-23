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
package com.baomidou.dynamic.datasource.fixture.v1;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.fixture.v1.service.nest.SchoolService;
import com.baomidou.dynamic.datasource.fixture.v1.service.nest.Student;
import com.baomidou.dynamic.datasource.fixture.v1.service.nest.StudentService;
import com.baomidou.dynamic.datasource.fixture.v1.service.nest.TeacherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;

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
        DataSourceProperty masterDataSourceProperty = createDataSourceProperty("master");
        DataSourceProperty teacherDataSourceProperty = createDataSourceProperty("teacher");
        DataSourceProperty studentDataSourceProperty = createDataSourceProperty("student");
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        ds.addDataSource(masterDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(masterDataSourceProperty));
        ds.addDataSource(teacherDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(teacherDataSourceProperty));
        ds.addDataSource(studentDataSourceProperty.getPoolName(), dataSourceCreator.createDataSource(studentDataSourceProperty));
        assertThat(ds.getDataSources().keySet()).contains("master", "teacher", "student");
        assertThat(teacherService.addTeacherWithTx("ss", 1)).isEqualTo(1);
        assertThat(studentService.addStudentWithTx("tt", 2)).isEqualTo(1);
        assertThat(teacherService.selectTeachers()).isEmpty();
        assertThat(studentService.selectStudents()).isEqualTo(Collections.singletonList(new Student(1, "tt", 2)));
        assertThat(schoolService.addTeacherAndStudentWithTx()).isEqualTo(2);
        assertThat(teacherService.selectTeachers()).isEmpty();
        assertThat(studentService.selectStudents()).isEqualTo(Arrays.asList(new Student(1, "tt", 2), new Student(2, "bb", 4)));
    }

    private DataSourceProperty createDataSourceProperty(String poolName) {
        DataSourceProperty result = new DataSourceProperty();
        result.setPoolName(poolName);
        result.setDriverClassName("org.h2.Driver");
        result.setUrl("jdbc:h2:mem:" + poolName + ";INIT=RUNSCRIPT FROM 'classpath:db/add-remove-datasource.sql'");
        result.setUsername("sa");
        result.setPassword("");
        return result;
    }
}

@SpringBootApplication
class NestApplication {
    public static void main(String[] args) {
        SpringApplication.run(NestApplication.class, args);
    }
}