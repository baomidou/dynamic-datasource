package com.baomidou.samples.seata.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.samples.seata.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderDao extends BaseMapper<Order> {

}