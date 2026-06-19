package com.example.authority.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

import com.example.authority.service.ArticleService;
import com.example.authority.entity.Article;
import com.example.authority.service.UserService;
import com.example.authority.entity.User;
import com.example.authority.service.CollectService;
import com.example.authority.entity.Collect;


/**
 * @program: design
 * @ClassName:CollectController
 * @description: CollectController前端控制器
 * @author:design
 * @Version 3.0
 **/
@Tag(name = "收藏列表 前端控制器")
@RestController
@RequestMapping("/collect")
public class CollectController {

    @Resource
    private CollectService collectService;
    @Resource
    private ArticleService articleService;
    @Resource
    private UserService userService;

    /**
     * 新增
     *
     * @param collect
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping
    public Result save(@RequestBody Collect collect) {
        collect.setUserId(StpUtil.getLoginIdAsLong());
        if (collectService.list(
                new LambdaUpdateWrapper<Collect>().eq(Collect::getUserId,StpUtil.getLoginIdAsLong()).eq(Collect::getArticleId,collect.getArticleId())
        ).size()>0){
            collectService.remove(
                    new LambdaUpdateWrapper<Collect>().eq(Collect::getUserId,StpUtil.getLoginIdAsLong()).eq(Collect::getArticleId,collect.getArticleId())
            );
            return Result.success("取消收藏成功");
        }else{
            collectService.save(collect);
            return Result.success("收藏成功");
        }

    }

    /**
     * 修改
     *
     * @param collect
     * @return
     */
    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Collect collect) {
        return Result.success(collectService.updateById(collect));
    }

    /**
     * 查询所有Collect
     *
     * @return
     */
    @Operation(summary = "查询所有Collect")
    @GetMapping
    public Result findAll() {
        return Result.success(collectService.list());
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
        return Result.success(collectService.getById(id));
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
            @RequestParam(defaultValue = "") Long articleId,
            @RequestParam(defaultValue = "") Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper();
        if (articleId != null && articleId != 0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Collect::getArticleId, articleId);
        }
        if (userId != null && userId != 0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Collect::getUserId, userId);
        }
        if (StpUtil.hasRole("user")) {
            queryWrapper.eq(Collect::getUserId, StpUtil.getLoginIdAsLong());
        }
        queryWrapper.orderByDesc(Collect::getId);
        Page<Collect> page = collectService.page(new Page<>(pageNum, pageSize), queryWrapper);
        List<Collect> records = page.getRecords();
        for (Collect collect : records) {
            Article article = articleService.getById(collect.getArticleId());
            if (ObjectUtil.isNotEmpty(article)) {
                collect.setArticle(article);
            }
            User user = userService.getById(collect.getUserId());
            if (ObjectUtil.isNotEmpty(user)) {
                collect.setUser(user);
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
        return Result.success(collectService.removeById(id));
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
        return Result.success(collectService.removeByIds(Arrays.asList(ids)));
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

        List<Collect> list = collectService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("collect导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Collect.class).sheet("sheel1").doWrite(list);

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
        EasyExcel.read(file.getInputStream(), Collect.class, new PageReadListener<Collect>(dataList -> {
            dataList.forEach(entity -> collectService.save(entity.toBuilder().id(null).build()));
        })).sheet().doRead();
        return Result.success();
    }

}