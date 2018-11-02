package com.baomidou.samples.druid.mybatis.service.impl;


import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.samples.druid.mybatis.mapper.TeacherMapper;
import com.baomidou.samples.druid.mybatis.service.TeacherService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@DS("slave")
public class TeacherServiceImpl implements TeacherService {

    @Resource
    private TeacherMapper teacherMapper;

    @Override
    public List selectTeachers() {
        return teacherMapper.selectTeachers();
    }
}
