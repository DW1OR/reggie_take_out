package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

/**
 * 订单表 服务类
 */
public interface OrdersService extends IService<Orders> {
    //用户下单
    void submit(Orders orders);
}