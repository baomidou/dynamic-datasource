package com.baomidou.samples.seata.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.samples.seata.entity.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountDao extends BaseMapper<Account> {

}