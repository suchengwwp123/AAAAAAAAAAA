package com.example.authority.controller;
import cn.dev33.satoken.stp.StpUtil;
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
import com.example.authority.service.OrderService;
import com.example.authority.entity.Order ;
import com.example.authority.service.VitaeService;
import com.example.authority.entity.Vitae ;
import com.example.authority.service.TicketsService;
import com.example.authority.entity.Tickets;



/**
 * @program: design
 * @ClassName:TicketsController
 * @description: TicketsController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = "工单记录 前端控制器")
    @RestController
@RequestMapping("/tickets")
    public class TicketsController {

@Resource
private TicketsService ticketsService;
@Resource
private UserService userService;
@Resource
private OrderService orderService;
@Resource
private VitaeService vitaeService;

/**
 * 新增
 * @param tickets
 * @return
 */
@Operation(summary = "新增")
@PostMapping
public Result save(@RequestBody Tickets tickets){
        return Result.success(ticketsService.save(tickets));
        }
/**
* 修改
*
* @param tickets
* @return
*/
@Operation(summary = "修改")
@PutMapping("/{id}")
public Result update(@PathVariable Long id,@RequestBody Tickets tickets) {
        return Result.success(ticketsService.updateById(tickets));
        }

/**
 * 查询所有Tickets
 * @return
 */
@Operation(summary = "查询所有Tickets")
@GetMapping
public Result findAll(){
        return Result.success(ticketsService.list());
        }
/**
  * 获取单个
  * @param id
  * @return
  */
@Operation(summary = "获取单个")
@GetMapping("/{id}")
public Result findOne(@PathVariable Integer id){
        return Result.success(ticketsService.getById(id));
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
        @RequestParam(defaultValue = "") Long userId,
        @RequestParam(defaultValue = "") Long orderId,
        @RequestParam(defaultValue = "") String statu,
        @RequestParam(defaultValue = "") Long vaId,
@RequestParam(defaultValue = "1") Integer pageNum,
@RequestParam(defaultValue = "10") Integer pageSize){

        LambdaQueryWrapper<Tickets>queryWrapper=new LambdaQueryWrapper();
        if (userId!=null&&userId!=0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Tickets::getUserId,userId);
        }
        if (orderId!=null&&orderId!=0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Tickets::getOrderId,orderId);
        }
        if (StrUtil.isNotEmpty(statu)) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.like(Tickets::getStatu,statu);
        }
        if (vaId!=null&&vaId!=0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Tickets::getVaId,vaId);
        }

        if (StpUtil.hasRole("user")){
            queryWrapper.eq(Tickets::getUserId,StpUtil.getLoginIdAsLong());
        }

        queryWrapper.orderByDesc(Tickets::getId);
        Page<Tickets>page=ticketsService.page(new Page<>(pageNum,pageSize),queryWrapper);
        List<Tickets > records = page.getRecords();
         for (Tickets tickets : records) {
                 User user = userService.getById(tickets.getUserId());
                 if (ObjectUtil.isNotEmpty(user)){
                     tickets.setUser(user);
                 }
                 Order order = orderService.getById(tickets.getOrderId());
                 if (ObjectUtil.isNotEmpty(order)){
                     tickets.setOrder(order);
                 }
                 Vitae vitae = vitaeService.getById(tickets.getVaId());
                 if (ObjectUtil.isNotEmpty(vitae)){
                     tickets.setVitae(vitae);
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
        return Result.success(ticketsService.removeById(id));
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
        return Result.success(ticketsService.removeByIds(Arrays.asList(ids)));
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

        List<Tickets> list = ticketsService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("tickets导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Tickets.class).sheet("sheel1").doWrite(list);

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
        EasyExcel.read(file.getInputStream(), Tickets.class, new PageReadListener<Tickets>(dataList -> {
        dataList.forEach(entity -> ticketsService.save(entity.toBuilder().id(null).build()));
        })).sheet().doRead();
        return Result.success();
        }

        }