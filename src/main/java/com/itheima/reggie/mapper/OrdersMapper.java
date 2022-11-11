package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单表 Mapper 接口
 */
@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}