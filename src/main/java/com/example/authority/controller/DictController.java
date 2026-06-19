package com.example.authority.controller;

import com.alibaba.excel.EasyExcel;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.authority.common.Result;
import com.example.authority.entity.dto.Options;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.example.authority.service.DictService;
import com.example.authority.entity.Dict;


/**
 * @program: design
 * @ClassName:DictController
 * @description: DictController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = "数据字典 前端控制器")
@RestController
@RequestMapping("/dict")
public class DictController {

    @Resource
    private DictService dictService;

    /**
     * 新增
     *
     * @param dict
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping
    public Result save(@RequestBody Dict dict) {

        if (dict.getPid() == 0L) {
            if (dictService.list(
                    new LambdaQueryWrapper<Dict>().eq(Dict::getIdentification, dict.getIdentification())
            ).size() > 0) {
                throw new RuntimeException("唯一标识不能重复！");
            }
        } else {
            if (dictService.list(
                    new LambdaQueryWrapper<Dict>().eq(Dict::getAssignments, dict.getAssignments()).eq(Dict::getPid, dict.getPid())
            ).size() > 0) {
                throw new RuntimeException("键值不能重复！");
            }
        }

        return Result.success(dictService.save(dict));
    }


    /**
     * 修改
     *
     * @param dict
     * @return
     */
    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Dict dict) {
        if (dict.getPid() == 0L) {
            if (dictService.list(
                    new LambdaQueryWrapper<Dict>().eq(Dict::getIdentification, dict.getIdentification()).ne(Dict::getId, dict.getId())
            ).size() > 0) {
                throw new RuntimeException("唯一标识不能重复！");
            }
        } else {
            if (dictService.list(
                    new LambdaQueryWrapper<Dict>().eq(Dict::getAssignments, dict.getAssignments()).eq(Dict::getPid, dict.getPid()).ne(Dict::getId, dict.getId())
            ).size() > 0) {
                throw new RuntimeException("键值不能重复！");
            }
        }
        return Result.success(dictService.updateById(dict));
    }

    /**
     * 查询所有Dict
     *
     * @return
     */
    @Operation(summary = "查询所有Dict")
    @GetMapping
    public Result findAll() {
        return Result.success(dictService.list(


        ));
    }

    /**
     * 查询所有的Dict
     *
     * @return
     */
    @Operation(summary = "查询所有Dict转成Options的数据类型")
    @GetMapping("/options")
    public Result findDictOptions() {
        List<Dict> dicts = dictService.list(

                new LambdaQueryWrapper<Dict>().eq(Dict::getStatu,1).eq(Dict::getPid,0).orderByDesc(Dict::getId)
        );
        List<Options> optionsList=new ArrayList<>();
        for (Dict dict:dicts){
            optionsList.add(new Options().toBuilder().
                    label(dict.getName()+"  "+dict.getIdentification()).value(String.valueOf(dict.getId())).
                    build());
        }
        return Result.success(optionsList);
    }

    /**
     * 根据字典查询列表
     *
     * @return
     */
    @Operation(summary = "查询所有Dict根据Pid")
    @GetMapping("/detail/{pid}")
    public Result findDictsByPid(@PathVariable Long pid) {
        Dict dict = dictService.getById(pid);
        if (dict.getStatu() == 1) {
            return Result.success(
                    dictService.list(
                            new LambdaQueryWrapper<Dict>().eq(Dict::getPid, pid).eq(Dict::getStatu, 1)
                                    .orderByAsc(Dict::getSorts)
                    )
            );
        } else {
            return Result.success(new ArrayList<>());
        }

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
        return Result.success(dictService.getById(id));
    }

    /**
     * 分页显示
     *
     * @param
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Operation(summary = "分页显示")
    @GetMapping("/page")
    public Result findPage(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String identification,
            @RequestParam(defaultValue = "") Integer statu,
            @RequestParam(defaultValue = "") Long pid,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<Dict> queryWrapper = new LambdaQueryWrapper();
        if (StrUtil.isNotEmpty(name)) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.like(Dict::getName, name);
        }
        if (StrUtil.isNotEmpty(identification)) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.like(Dict::getIdentification, identification);
        }

        if (statu != null && statu != 0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Dict::getStatu, statu);
        }

        if (pid != null) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Dict::getPid, pid);
        }


        queryWrapper.orderByAsc(Dict::getSorts);
        queryWrapper.orderByDesc(Dict::getId);
        return Result.success(dictService.page(new Page<>(pageNum, pageSize), queryWrapper));

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
        this.batchDeleteChildRens(id);
        return Result.success(dictService.removeById(id));
    }

    /**
     * 批量删除字典详情
     *
     * @param pid
     */
    private void batchDeleteChildRens(Object pid) {
        dictService.remove(new LambdaQueryWrapper<Dict>().eq(Dict::getPid, pid));
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
        for (String id : ids) {
            this.batchDeleteChildRens(id);
        }
        return Result.success(dictService.removeByIds(Arrays.asList(ids)));
    }

    /**
     * 批量导出
     * 使用的技术为alibaba下面的easyexcel
     * 写数据
     *
     * @param ids
     * @return
     */
    @Operation(summary = "批量导出")
    @GetMapping("/batch/export/{ids}")
    public void exportByIds(@PathVariable String[] ids, HttpServletResponse response) throws IOException {

        List<Dict> list = dictService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("dict导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Dict.class).sheet("sheel1").doWrite(list);

    }

    /**
     * 批量导入
     * 使用的技术为alibaba下面的easyexcel
     * 读数据
     *
     * @param
     */
    @Operation(summary = "批量导入")
    @PostMapping("/batch/upload")
    public Result writeExcel(MultipartFile file) throws IOException {
        EasyExcel.read(file.getInputStream(), Dict.class, new PageReadListener<Dict>(dataList -> {
            dataList.forEach(entity -> dictService.save(entity.toBuilder().id(null).build()));
        })).sheet().doRead();
        return Result.success();
    }

}