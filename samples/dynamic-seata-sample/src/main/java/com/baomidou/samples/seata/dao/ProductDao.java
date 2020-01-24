package com.baomidou.samples.seata.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.samples.seata.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductDao extends BaseMapper<Product> {

}