package com.baomidou.samples.transaction.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.samples.transaction.entity.A;
import com.baomidou.samples.transaction.mapper.MapperA;
import org.springframework.stereotype.Service;

/**
 * @author Hccake 2020/9/3
 * @version 1.0
 */
@DS("a")
@Service
public class ServiceA  extends ServiceImpl<MapperA, A> {
}
