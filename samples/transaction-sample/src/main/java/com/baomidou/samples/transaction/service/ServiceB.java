package com.baomidou.samples.transaction.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.samples.transaction.entity.B;
import com.baomidou.samples.transaction.mapper.MapperB;
import org.springframework.stereotype.Service;

/**
 * @author Hccake 2020/9/3
 * @version 1.0
 */
@DS("b")
@Service
public class ServiceB extends ServiceImpl<MapperB, B> {
}
