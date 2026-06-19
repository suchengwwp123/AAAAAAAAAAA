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

import com.example.authority.service.OrderService;
import com.example.authority.entity.Order;
import com.example.authority.service.UserService;
import com.example.authority.entity.User;
import com.example.authority.service.VitaeService;
import com.example.authority.entity.Vitae;
import com.example.authority.service.ComplainService;
import com.example.authority.entity.Complain;


/**
 * @program: design
 * @ClassName:ComplainController
 * @description: ComplainController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = "投诉记录 前端控制器")
@RestController
@RequestMapping("/complain")
public class ComplainController {

    @Resource
    private ComplainService complainService;
    @Resource
    private OrderService orderService;
    @Resource
    private UserService userService;
    @Resource
    private VitaeService vitaeService;

    /**
     * 新增
     *
     * @param complain
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping
    public Result save(@RequestBody Complain complain) {
        return Result.success(complainService.save(complain));
    }

    /**
     * 修改
     *
     * @param complain
     * @return
     */
    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Complain complain) {
        return Result.success(complainService.updateById(complain));
    }

    /**
     * 查询所有Complain
     *
     * @return
     */
    @Operation(summary = "查询所有Complain")
    @GetMapping
    public Result findAll() {
        return Result.success(complainService.list());
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
        return Result.success(complainService.getById(id));
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
            @RequestParam(defaultValue = "") Long orderId,
            @RequestParam(defaultValue = "") Long userId,
            @RequestParam(defaultValue = "") Long vaId,
            @RequestParam(defaultValue = "") String statu,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<Complain> queryWrapper = new LambdaQueryWrapper();
        if (orderId != null && orderId != 0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Complain::getOrderId, orderId);
        }
        if (userId != null && userId != 0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Complain::getUserId, userId);
        }
        if (vaId != null && vaId != 0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Complain::getVaId, vaId);
        }
        if (StrUtil.isNotEmpty(statu)) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.like(Complain::getStatu, statu);
        }
        if (StpUtil.hasRole("user")){
            queryWrapper.eq(Complain::getUserId, StpUtil.getLoginIdAsLong());
        }

        queryWrapper.orderByDesc(Complain::getId);
        Page<Complain> page = complainService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Complain> records = page.getRecords();
        for (Complain complain : records) {
            Order order = orderService.getById(complain.getOrderId());
            if (ObjectUtil.isNotEmpty(order)) {
                complain.setOrder(order);
            }
            User user = userService.getById(complain.getUserId());
            if (ObjectUtil.isNotEmpty(user)) {
                complain.setUser(user);
            }
            Vitae vitae = vitaeService.getById(complain.getVaId());
            if (ObjectUtil.isNotEmpty(vitae)) {
                complain.setVitae(vitae);
            }
        }
        page.setRecords(records);
        return Result.success(page);

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
        return Result.success(complainService.removeById(id));
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
        return Result.success(complainService.removeByIds(Arrays.asList(ids)));
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

        List<Complain> list = complainService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("complain导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Complain.class).sheet("sheel1").doWrite(list);

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
        EasyExcel.read(file.getInputStream(), Complain.class, new PageReadListener<Complain>(dataList -> {
            dataList.forEach(entity -> complainService.save(entity.toBuilder().id(null).build()));
        })).sheet().doRead();
        return Result.success();
    }

}