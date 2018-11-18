package com.baomidou.samples.nest.controller;

import com.baomidou.samples.nest.service.SchoolService;
import com.baomidou.samples.nest.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @Autowired
    private TeacherService teacherService;

    @Autowired
    private SchoolService schoolService;

    @GetMapping("/tx1")
    public void tx1() {
        //外层不加事物，里面每个单独加事物（支持，不过感觉没毛用）
        schoolService.addTeacherAndStudent();
    }

    @GetMapping("/tx2")
    public void tx2() {
        //外层加事物，里面每个也加事物（不支持!!）其实只要加了事物就不支持多库
        schoolService.addTeacherAndStudentWithTx();
    }

    @GetMapping("/tx3")
    public void tx3() {
        //单独调用加事物单库（支持）
        teacherService.addTeacherWithTx("tt", 12);
    }
}
