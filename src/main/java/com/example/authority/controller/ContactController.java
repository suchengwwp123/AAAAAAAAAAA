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

import com.example.authority.service.ContactService;
import com.example.authority.entity.Contact;



/**
 * @program: design
 * @ClassName:ContactController
 * @description: ContactController前端控制器
 * @author:design
 * @Version 3.0
 **/
@Tag(name = "联系记录 前端控制器")
@RestController
@RequestMapping("/contact")
public class ContactController {

        @Resource
        private ContactService contactService;

        /**
         * 新增
         * @param contact
         * @return
         */
        @Operation(summary = "新增")
        @PostMapping
        public Result save(@RequestBody Contact contact){
                return Result.success(contactService.save(contact));
        }
        /**
         * 修改
         *
         * @param contact
         * @return
         */
        @Operation(summary = "修改")
        @PutMapping("/{id}")
        public Result update(@PathVariable Long id,@RequestBody Contact contact) {
                return Result.success(contactService.updateById(contact));
        }

        /**
         * 查询所有Contact
         * @return
         */
        @Operation(summary = "查询所有Contact")
        @GetMapping
        public Result findAll(){
                return Result.success(contactService.list());
        }
        /**
         * 获取单个
         * @param id
         * @return
         */
        @Operation(summary = "获取单个")
        @GetMapping("/{id}")
        public Result findOne(@PathVariable Integer id){
                return Result.success(contactService.getById(id));
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
                @RequestParam(defaultValue = "") String statu,
                @RequestParam(defaultValue = "1") Integer pageNum,
                @RequestParam(defaultValue = "10") Integer pageSize){

                LambdaQueryWrapper<Contact>queryWrapper=new LambdaQueryWrapper();
                if (StrUtil.isNotEmpty(name)) {
                        // name 不为空且不是空字符串的处理逻辑
                        queryWrapper.like(Contact::getName,name);
                }
                if (StrUtil.isNotEmpty(statu)) {
                        // name 不为空且不是空字符串的处理逻辑
                        queryWrapper.like(Contact::getStatu,statu);
                }

                queryWrapper.orderByDesc(Contact::getId);
                return Result.success(contactService.page(new Page<>(pageNum,pageSize),queryWrapper));

        }
        /**
         * 单个删除
         * @param id
         * @return
         */
        @Operation(summary = "单个删除")
        @DeleteMapping("/{id}")
        public Result delete(@PathVariable Integer id){
                return Result.success(contactService.removeById(id));
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
                return Result.success(contactService.removeByIds(Arrays.asList(ids)));
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

                List<Contact> list = contactService.listByIds(Arrays.asList(ids));
                // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("utf-8");
                // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
                String fileName = URLEncoder.encode("contact导出数据", "UTF-8").replaceAll("\\+", "%20");
                response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
                EasyExcel.write(response.getOutputStream(), Contact.class).sheet("sheel1").doWrite(list);

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
                EasyExcel.read(file.getInputStream(), Contact.class, new PageReadListener<Contact>(dataList -> {
                        dataList.forEach(entity -> contactService.save(entity.toBuilder().id(null).build()));
                })).sheet().doRead();
                return Result.success();
        }

}