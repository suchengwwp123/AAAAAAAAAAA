package com.example.authority.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.authority.common.Result;
import com.example.authority.entity.Comment;

import com.example.authority.entity.User;
import com.example.authority.service.CommentService;

import com.example.authority.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @program:
 * @ClassName:CommentController
 * @description: CommentController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = "评论管理 前端控制器")
@RestController
@RequestMapping("/comment")
public class CommentController {

        @Resource
        private CommentService commentService;
        @Resource
        private UserService userService;


        /**
         * 新增
         *
         * @param comment
         * @return
         */
        @Operation(summary = "新增")
        @PostMapping
        public Result save(@RequestBody Comment comment) {



                return Result.success(commentService.save(comment));
        }

        /**
         * 修改
         *
         * @param comment
         * @return
         */
        @Operation(summary = "修改")
        @PutMapping("/{id}")
        public Result update(@PathVariable Long id, @RequestBody Comment comment) {
                return Result.success(commentService.updateById(comment));
        }

        /**
         * 查询所有Comment
         *
         * @return
         */
        @Operation(summary = "查询所有Comment")
        @GetMapping
        public Result findAll() {
                return Result.success(commentService.list());
        }

        /**
         * 查看评论
         * @param id
         * @return
         */
        @Operation(summary = "查询所有Comment")
        @GetMapping("/article/{id}")
        public Result findByArti(@PathVariable Long id) {
                List<Comment> commentList = commentService.list(new LambdaQueryWrapper<Comment>().eq(Comment::getAid, id).orderByDesc(Comment::getId));
                List<Comment> comments = new ArrayList<>();
                for (Comment comment : commentList) {
                        User user = userService.getById(comment.getUid());
                        if (ObjectUtil.isNotEmpty(user)) {
                                comment.setUser(user);
                        }
                        for (Comment sonComment : commentList) {
                                if (sonComment.getParentId().equals(comment.getId())) {
                                        comment.getReply().setTotal(comment.getReply().getTotal() + 1);
                                        comment.getReply().getList().add(sonComment);
                                }
                        }
                        if (comment.getParentId() == 0L) {
                                comments.add(comment);
                        }
                }

                return Result.success(comments);
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
                return Result.success(commentService.getById(id));
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
                @RequestParam(defaultValue = "") String aid,
                @RequestParam(defaultValue = "1") Integer pageNum,
                @RequestParam(defaultValue = "10") Integer pageSize) {
                QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
                if (!"".equals(name)) {
                        queryWrapper.like("content", name);

                }
                if (aid!=null){
                        queryWrapper.eq("aid",aid);
                }
                queryWrapper.orderByDesc("id");
                return Result.success(commentService.page(new Page<>(pageNum, pageSize), queryWrapper));
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

                return Result.success(commentService.removeById(id));
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
                return Result.success(commentService.removeByIds(Arrays.asList(ids)));
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

                List<Comment> list = commentService.listByIds(Arrays.asList(ids));
                // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setCharacterEncoding("utf-8");
                // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
                String fileName = URLEncoder.encode("comment导出数据", "UTF-8").replaceAll("\\+", "%20");
                response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
                EasyExcel.write(response.getOutputStream(), Comment.class).sheet("sheel1").doWrite(list);

        }



}