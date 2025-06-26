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

import com.example.authority.service.DictService;
import com.example.authority.entity.Dict ;
//import com.example.authority.service.GradeService;
//import com.example.authority.entity.Grade ;
import com.example.authority.service.StudentService;
import com.example.authority.entity.Student;



/**
 * @program: authority-2026.0.2
 * @ClassName:StudentController
 * @description: StudentController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = "学生列表 前端控制器")
    @RestController
@RequestMapping("/student")
    public class StudentController {

@Resource
private StudentService studentService;
@Resource
private DictService sexDictService;
//@Resource
//private GradeService gradeService;

/**
 * 新增
 * @param student
 * @return
 */
@Operation(summary = "新增")
@PostMapping
public Result save(@RequestBody Student student){
        return Result.success(studentService.save(student));
        }
/**
* 修改
*
* @param student
* @return
*/
@Operation(summary = "修改")
@PutMapping("/{id}")
public Result update(@PathVariable Long id,@RequestBody Student student) {
        return Result.success(studentService.updateById(student));
        }

/**
 * 查询所有Student
 * @return
 */
@Operation(summary = "查询所有Student")
@GetMapping
public Result findAll(){
        return Result.success(studentService.list());
        }
/**
  * 获取单个
  * @param id
  * @return
  */
@Operation(summary = "获取单个")
@GetMapping("/{id}")
public Result findOne(@PathVariable Integer id){
        return Result.success(studentService.getById(id));
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
        @RequestParam(defaultValue = "") String name,
        @RequestParam(defaultValue = "") Long sex,
        @RequestParam(defaultValue = "") Long gid,
@RequestParam(defaultValue = "1") Integer pageNum,
@RequestParam(defaultValue = "10") Integer pageSize){

        LambdaQueryWrapper<Student>queryWrapper=new LambdaQueryWrapper();
        if (StrUtil.isNotEmpty(name)) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.like(Student::getName,name);
        }
        if (sex!=null&&sex!=0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Student::getSex,sex);
        }
        if (gid!=null&&gid!=0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Student::getGid,gid);
        }

        queryWrapper.orderByDesc(Student::getId);
        Page<Student>page=studentService.page(new Page<>(pageNum,pageSize),queryWrapper);
        List<Student > records = page.getRecords();
         for (Student student : records) {
                 Dict sexDict = sexDictService.getById(student.getSex());
                 if (ObjectUtil.isNotEmpty(sexDict)){
                     student.setSexDict(sexDict);
                 }
//                 Grade grade = gradeService.getById(student.getGid());
//                 if (ObjectUtil.isNotEmpty(grade)){
//                     student.setGrade(grade);
//                 }
         }
         page.setRecords(records);
         return Result.success(page);

        }
/**
* 单个删除
* @param id
* @return
*/
@Operation(summary = "单个删除")
@DeleteMapping("/{id}")
public Result delete(@PathVariable Integer id){
        return Result.success(studentService.removeById(id));
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
        return Result.success(studentService.removeByIds(Arrays.asList(ids)));
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

        List<Student> list = studentService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("student导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Student.class).sheet("sheel1").doWrite(list);

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
        EasyExcel.read(file.getInputStream(), Student.class, new PageReadListener<Student>(dataList -> {
        dataList.forEach(entity -> studentService.save(entity.toBuilder().id(null).build()));
        })).sheet().doRead();
        return Result.success();
        }

        }