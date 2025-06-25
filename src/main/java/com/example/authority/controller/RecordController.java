package com.example.authority.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.authority.common.Result;
import com.example.authority.entity.Record;
import com.example.authority.service.RecordService;
import com.example.authority.service.UserService;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @program: authority-2026.0.1
 * @ClassName:RecordController
 * @description: RecordController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = " 前端控制器")
@RestController
@RequestMapping("/record")
public class RecordController {

    @Resource
    private RecordService recordService;
    @Resource
    private UserService userService;

    /**
     * 新增
     *
     * @param record
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping
    public Result save(@RequestBody Record record) {
        return Result.success(recordService.save(record));
    }

    /**
     * 修改
     *
     * @param record
     * @return
     */
    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Record record) {
        return Result.success(recordService.updateById(record));
    }

    /**
     * 查询所有Record
     *
     * @return
     */
    @Operation(summary = "查询所有Record")
    @GetMapping
    public Result findAll() {
        return Result.success(recordService.list());
    }

    /**
     * 获取单个
     *
     * @param id
     * @return
     */
    @Operation(summary = "获取单个")
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(recordService.getById(id));
    }

    /**
     * 分页显示
     *
     * @param name
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Operation(summary = "分页显示")
    @GetMapping("/page")
    public Result findPage(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<Record> queryWrapper=new LambdaQueryWrapper<>();

            queryWrapper.like(Record::getTableName, name).orderByDesc(Record::getId);

        Page<Record> page= recordService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Record> records=page.getRecords();
        records.stream().map(record -> record.setUser(userService.getById(record.getUid()))).collect(Collectors.toList());
        return Result.success(page.setRecords(records));
    }

    /**
     * 单个删除
     *
     * @param id
     * @return
     */
    @Operation(summary = "单个删除")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(recordService.removeById(id));
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("/batch/{ids}")
    @Transactional
    public Result deleteByIds(@PathVariable String[] ids) {
        return Result.success(recordService.removeByIds(Arrays.asList(ids)));
    }
}

