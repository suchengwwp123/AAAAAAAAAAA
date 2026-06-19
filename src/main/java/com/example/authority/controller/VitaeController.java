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

import com.example.authority.service.UserService;
import com.example.authority.entity.User ;
import com.example.authority.service.VitaeService;
import com.example.authority.entity.Vitae;



/**
 * @program: design
 * @ClassName:VitaeController
 * @description: VitaeController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = "简历管理 前端控制器")
    @RestController
@RequestMapping("/vitae")
    public class VitaeController {

@Resource
private VitaeService vitaeService;
@Resource
private UserService userService;

/**
 * 新增
 * @param vitae
 * @return
 */
@Operation(summary = "新增")
@PostMapping
public Result save(@RequestBody Vitae vitae){
        return Result.success(vitaeService.save(vitae));
        }
/**
* 修改
*
* @param vitae
* @return
*/
@Operation(summary = "修改")
@PutMapping("/{id}")
public Result update(@PathVariable Long id,@RequestBody Vitae vitae) {
        return Result.success(vitaeService.updateById(vitae));
        }

/**
 * 查询所有Vitae
 * @return
 */
@Operation(summary = "查询所有Vitae")
@GetMapping
public Result findAll(){
        return Result.success(vitaeService.list());
        }
/**
  * 获取单个
  * @param id
  * @return
  */
@Operation(summary = "获取单个")
@GetMapping("/{id}")
public Result findOne(@PathVariable Integer id){
        return Result.success(vitaeService.getById(id));
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
        @RequestParam(defaultValue = "") Long userId,
        @RequestParam(defaultValue = "") String address,
@RequestParam(defaultValue = "1") Integer pageNum,
@RequestParam(defaultValue = "10") Integer pageSize){

        LambdaQueryWrapper<Vitae>queryWrapper=new LambdaQueryWrapper();
        if (StrUtil.isNotEmpty(name)) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.like(Vitae::getName,name);
        }
        if (userId!=null&&userId!=0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Vitae::getUserId,userId);
        }
        if (StrUtil.isNotEmpty(address)) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.like(Vitae::getAddress,address);
        }

        queryWrapper.orderByDesc(Vitae::getId);
        Page<Vitae>page=vitaeService.page(new Page<>(pageNum,pageSize),queryWrapper);
        List<Vitae > records = page.getRecords();
         for (Vitae vitae : records) {
                 User user = userService.getById(vitae.getUserId());
                 if (ObjectUtil.isNotEmpty(user)){
                     vitae.setUser(user);
                 }
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
        return Result.success(vitaeService.removeById(id));
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
        return Result.success(vitaeService.removeByIds(Arrays.asList(ids)));
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

        List<Vitae> list = vitaeService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("vitae导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Vitae.class).sheet("sheel1").doWrite(list);

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
        EasyExcel.read(file.getInputStream(), Vitae.class, new PageReadListener<Vitae>(dataList -> {
        dataList.forEach(entity -> vitaeService.save(entity.toBuilder().id(null).build()));
        })).sheet().doRead();
        return Result.success();
        }

        }