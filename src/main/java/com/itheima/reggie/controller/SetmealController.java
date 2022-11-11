package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.Result;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息：{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return Result.success("新增套餐成功");
    }

    /**
     * 套餐信息分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name) {
        //构建分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        //构建条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        //执行查询
        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝
        //records为不进行拷贝的内容，是呈现在列表上的数据，需要进行处理后再拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        //获取处理前的列表数据
        List<Setmeal> records = pageInfo.getRecords();
        //转换为流进行处理
        List<SetmealDto> list = records.stream().map((item) -> {
            //创建SetmealDto对象，用于接收处理值
            SetmealDto setmealDto = new SetmealDto();
            //进行属性复制
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);

            //当查询到的分类对象不为空时
            if (category != null) {
                //获取分类名
                String categoryName = category.getName();
                //给setmealDto对象设置分类名称
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;
        }).collect(Collectors.toList());

        //将处理过的列表数据进行拷贝
        dtoPage.setRecords(list);

        return Result.success(dtoPage);
    }


    /**
     * 根据id查询套餐信息和菜品信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealDto> update(@PathVariable Long id) {

        SetmealDto setmealDto = setmealService.getByIdWithFlavor(id);

        return Result.success(setmealDto);
    }

    /**
     * 修改套餐
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithDish(setmealDto);

        return Result.success("修改套餐成功");
    }

    /**
     * 批量修改套餐状态为停售
     * 套餐状态与菜品状态不相互影响，减小关联性
     *
     * @param ids
     * @return
     */
    @PostMapping("/status/0")
    public Result<String> updateStatusToStop(String ids) {
        String[] strArr = ids.split(",");
        List<Setmeal> setmeals = new ArrayList<>();

        for (String s : strArr) {
            //获取修改套餐对象
            Setmeal setmeal = setmealService.getById(s);
            //修改套餐状态
            setmeal.setStatus(0);
            //获取修改套餐对象
            setmeals.add(setmeal);
        }

        //完成数据更新
        setmealService.updateBatchById(setmeals);

        return Result.success("套餐状态修改成功");
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
        List<Setmeal> setmeals = new ArrayList<>();

        for (String s : strArr) {
            //获取修改套餐对象
            Setmeal setmeal = setmealService.getById(s);
            //修改套餐状态
            setmeal.setStatus(1);
            //获取修改套餐对象
            setmeals.add(setmeal);
        }

        //完成数据更新
        setmealService.updateBatchById(setmeals);

        return Result.success("套餐状态修改成功");
    }

    /**
     * 根据id集合删除对应的套餐和菜品关联信息(此方法没使用逻辑删除，慎用)
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        setmealService.removeWithDish(ids);

        return Result.success("套餐数据删除成功");
    }

    /**
     * 消费者前台页面显示套餐相关的内容
     * 这里不能用RequestBody注解接收参数，是因为传来的参数不是完整的对象并且不是Json，只是对象的一部分
     * 用k-v形式进行传输，所以不能用RequestBody接收
     * @param setmeal
     * @return
     */
    @GetMapping("/list")  // 在消费者端 展示套餐信息
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public Result<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        Long categoryId = setmeal.getCategoryId();
        Integer status = setmeal.getStatus();
        //种类不为空才查
        queryWrapper.eq(categoryId != null,Setmeal::getCategoryId,categoryId);
        //在售状态才查
        queryWrapper.eq(status != null,Setmeal::getStatus,status);

        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmeals = setmealService.list(queryWrapper);

        return Result.success(setmeals);
    }
}
