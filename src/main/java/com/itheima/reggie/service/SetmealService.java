package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @Author Li Ye
 * @Version 1.0
 */

public interface SetmealService extends IService<Setmeal> {
    //新增套餐，同时需要报错套餐和菜品的关联关系
    void saveWithDish(SetmealDto setmealDto);

    //根据id查询套餐信息和对应的菜品信息
    SetmealDto getByIdWithFlavor(Long id);

    //更新套餐信息，同时更新对应的菜品信息
    void updateWithDish(SetmealDto setmealDto);

    //删除套餐，同时需要删除套餐和菜品的关联数据
    void removeWithDish(List<Long> ids);
}
