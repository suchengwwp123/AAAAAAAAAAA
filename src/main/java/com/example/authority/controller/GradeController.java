package com.example.authority.controller;
import com.alibaba.excel.EasyExcel;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.authority.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;

import com.example.authority.service.GradeService;
import com.example.authority.entity.Grade;



/**
 * @program: authority-2026.0.2
 * @ClassName:GradeController
 * @description: GradeController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = "班级列表 前端控制器")
    @RestController
@RequestMapping("/grade")
    public class GradeController {

@Resource
private GradeService gradeService;

/**
 * 新增
 * @param grade
 * @return
 */
@Operation(summary = "新增")
@PostMapping
public Result save(@RequestBody Grade grade){
        return Result.success(gradeService.save(grade));
        }
/**
* 修改
*
* @param grade
* @return
*/
@Operation(summary = "修改")
@PutMapping("/{id}")
public Result update(@PathVariable Long id,@RequestBody Grade grade) {
        return Result.success(gradeService.updateById(grade));
        }

/**
 * 查询所有Grade
 * @return
 */
@Operation(summary = "查询所有Grade")
@GetMapping
public Result findAll(){
        return Result.success(gradeService.list());
        }
/**
  * 获取单个
  * @param id
  * @return
  */
@Operation(summary = "获取单个")
@GetMapping("/{id}")
public Result findOne(@PathVariable Integer id){
        return Result.success(gradeService.getById(id));
        }
/**
 * 分页显示
 * @param
 * @param pageNum
 * @param pageSize
 * @return
 */
@Operation(summary = "分页显示")
@GetMapping("/page")
public Result findPage(
        @RequestParam(defaultValue = "") String grade_name,
@RequestParam(defaultValue = "1") Integer pageNum,
@RequestParam(defaultValue = "10") Integer pageSize){

        LambdaQueryWrapper<Grade>queryWrapper=new LambdaQueryWrapper();
        if (StrUtil.isNotEmpty(grade_name)) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.like(Grade::getGradeName,grade_name);
        }

        queryWrapper.orderByDesc(Grade::getId);
        return Result.success(gradeService.page(new Page<>(pageNum,pageSize),queryWrapper));

        }
/**
* 单个删除
* @param id
* @return
*/
@Operation(summary = "单个删除")
@DeleteMapping("/{id}")
public Result delete(@PathVariable Integer id){
        return Result.success(gradeService.removeById(id));
        }
/**
* 批量删除
* @param ids
* @return
*/
@Operation(summary = "批量删除")
@DeleteMapping("/batch/{ids}")
@Transactional
public Result deleteByIds(@PathVariable String[]ids){
        return Result.success(gradeService.removeByIds(Arrays.asList(ids)));
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

        List<Grade> list = gradeService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("grade导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Grade.class).sheet("sheel1").doWrite(list);

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
        EasyExcel.read(file.getInputStream(), Grade.class, new PageReadListener<Grade>(dataList -> {
        dataList.forEach(entity -> gradeService.save(entity.toBuilder().id(null).build()));
        })).sheet().doRead();
        return Result.success();
        }

        }