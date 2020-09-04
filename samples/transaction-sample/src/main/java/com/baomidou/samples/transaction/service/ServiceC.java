package com.baomidou.samples.transaction.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.samples.transaction.entity.C;
import com.baomidou.samples.transaction.mapper.MapperC;
import org.springframework.stereotype.Service;

/**
 * @author Hccake 2020/9/3
 * @version 1.0
 */
@DS("c")
@Service
public class ServiceC extends ServiceImpl<MapperC, C> {
}
