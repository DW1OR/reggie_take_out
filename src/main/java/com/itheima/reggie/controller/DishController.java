package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author Li Ye
 * @Version 1.0
 */

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        log.info("菜品信息：{}", dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return Result.success("新增菜品成功");
    }

    /**
     * 分类信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        log.info("page = {}, pageSize = {}, name = {}", page, pageSize, name);

        //构建分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //构建条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行查询
        dishService.page(pageInfo, queryWrapper);

        //对象拷贝
        //records为不进行拷贝的内容，是呈现在列表上的数据，需要进行处理后再拷贝
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        //获取处理前的列表数据
        List<Dish> records = pageInfo.getRecords();
        //转换为流进行处理
        List<DishDto> list = records.stream().map((item) -> {
            //创建dishDto对象，用于接收处理值
            DishDto dishDto = new DishDto();
            //进行属性复制
            BeanUtils.copyProperties(item, dishDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            //当查询到的分类对象不为空时
            if (category != null) {
                //获取分类名
                String categoryName = category.getName();
                //给dishDto对象设置分类名称
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());

        //将处理过的列表数据进行拷贝
        dishDtoPage.setRecords(list);

        return Result.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> update(@PathVariable Long id) {

        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return Result.success(dishDto);
    }

    /**
     * 修改菜品
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        log.info("菜品信息：{}", dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return Result.success("修改菜品成功");
    }

    /**
     * 批量修改菜品状态为停售
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public Result<String> updateStatusToStop(String ids) {
        String[] strArr = ids.split(",");
        List<Dish> dishes = new ArrayList<>();

        for (String s : strArr) {
            //获取修改菜品对象
            Dish dish = dishService.getById(s);
            //修改菜品状态
            dish.setStatus(0);
            //获取修改菜品对象
            dishes.add(dish);
        }

        //完成数据更新
        dishService.updateBatchById(dishes);

        return Result.success("菜品状态修改成功");
    }

    /**
     * 批量修改菜品状态为启售
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/1")
    public Result<String> updateStatusToStart(String ids) {
        String[] strArr = ids.split(",");
        List<Dish> dishes = new ArrayList<>();

        for (String s : strArr) {
            //获取修改菜品对象
            Dish dish = dishService.getById(s);
            //修改菜品状态
            dish.setStatus(1);
            //获取修改菜品对象
            dishes.add(dish);
        }

        //完成数据更新
        dishService.updateBatchById(dishes);

        return Result.success("菜品状态修改成功");
    }


    /**
     * 批量删除对应菜品(通过修改isDeleted属性，完成逻辑删除)
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> deleteDish(String ids) {
        String[] strArr = ids.split(",");
        List<Dish> dishes = new ArrayList<>();

        for (String s : strArr) {
            //获取修改菜品对象
            Dish dish = dishService.getById(s);
            //逻辑删除菜品
            dish.setIsDeleted(1);
            //获取修改菜品对象
            dishes.add(dish);
        }

        //完成数据更新
        dishService.updateBatchById(dishes);

        return Result.success("菜品删除成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public Result<List<DishDto>> lis(Dish dish) {
        //构建条件查询
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //添加条件，查询状态为1(起售状态)的菜品
        queryWrapper.eq(Dish::getStatus, 1);

        //添加排序排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);


        //转换为流进行处理
        List<DishDto> dishDtoList = list.stream().map((item) -> {
            //创建dishDto对象，用于接收处理值
            DishDto dishDto = new DishDto();
            //进行属性复制
            BeanUtils.copyProperties(item, dishDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            //当查询到的分类对象不为空时
            if (category != null) {
                //获取分类名
                String categoryName = category.getName();
                //给dishDto对象设置分类名称
                dishDto.setCategoryName(categoryName);
            }

            //追加口味数据，不会对后台造成影响
            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);

            //将查询到口味列表进行追加
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());

        return Result.success(dishDtoList);
    }
}
