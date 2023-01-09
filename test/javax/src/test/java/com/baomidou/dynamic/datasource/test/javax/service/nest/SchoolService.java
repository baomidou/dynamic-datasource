package com.baomidou.dynamic.datasource.test.javax.service.nest;

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
}
