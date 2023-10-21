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
package com.baomidou.dynamic.datasource.fixture.v3.service.nest;

import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchoolService {

    private final TeacherService teacherService;
    private final StudentService studentService;

    public SchoolService(TeacherService teacherService, StudentService studentService) {
        this.teacherService = teacherService;
        this.studentService = studentService;
    }

    @Transactional
    public int addTeacherAndStudentWithTx() {
        int aa = teacherService.addTeacherNoTx("aa", 3);
        int bb = studentService.addStudentNoTx("bb", 4);
        return aa + bb;
    }

    @DSTransactional
    public int addTeacherAndStudentWithDsTx() {
        int aa = teacherService.addTeacherNoTx("hh", 5);
        int bb = studentService.addStudentNoTx("zz", 6);
        return aa + bb;
    }

    @DSTransactional(rollbackFor = Exception.class)
    public int addTeacherAndStudentWithDsTxRollBack() {
        int aa = teacherService.addTeacherNoTx("hh", 5);
        int bb = studentService.addStudentNoTx("zz", 6);
        int i = 1 / 0;
        return aa + bb;
    }
}
