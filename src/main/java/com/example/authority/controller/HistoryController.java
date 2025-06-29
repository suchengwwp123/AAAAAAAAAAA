package com.example.authority.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.authority.common.Result;
import com.example.authority.entity.User;
import com.example.authority.entity.dto.ChatHistoryDto;
import com.example.authority.service.UserService;
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
import java.time.LocalDateTime;

import com.example.authority.service.HistoryService;
import com.example.authority.entity.History;


/**
 * @program: authority-2026.0.2
 * @ClassName:HistoryController
 * @description: HistoryController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = "聊天记录 前端控制器")
@RestController
@RequestMapping("/history")
public class HistoryController {

    @Resource
    private HistoryService historyService;
    @Resource
    private UserService userService;

    /**
     * 新增
     *
     * @param chatHistoryDto
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping
    public Result save(@RequestBody ChatHistoryDto chatHistoryDto) {
        History history = History.builder()
                .name(chatHistoryDto.getName())
                .datetime(chatHistoryDto.getDatetime())
                .content(chatHistoryDto.getContent())
                .role(chatHistoryDto.getRole())
                .sessionId(StpUtil.getLoginIdAsLong())
                .build();
        return Result.success(historyService.save(history));
    }

    /**
     * 修改
     *
     * @param history
     * @return
     */
    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody History history) {
        return Result.success(historyService.updateById(history));
    }

    /**
     * 查询所有History
     *
     * @return
     */
    @Operation(summary = "查询所有History")
    @GetMapping
    public Result findAll() {
        List<History> historys = historyService.list(
                new LambdaQueryWrapper<History>().eq(History::getSessionId, StpUtil.getLoginIdAsLong())
                        .orderByDesc(History::getId)
        );
        List<ChatHistoryDto> chatHistoryDtos = new ArrayList<>();
        for (History history : historys) {
            ChatHistoryDto chatHistoryDto;

            if ("user".equals(history.getRole())) {
                User user = userService.getById(history.getSessionId());
                chatHistoryDto = ChatHistoryDto.builder()
                        .avatar(user.getAvatar())
                        .name(history.getName())
                        .datetime(history.getDatetime())
                        .content(history.getContent())
                        .role(history.getRole())
                        .build();
            } else {
                chatHistoryDto = ChatHistoryDto.builder()
                        .avatar("https://tc.z.wiki/autoupload/GTpps48LCGRYU4_b7BixAPYRolZAPaDOGvgAk665t4Kyl5f0KlZfm6UsKj-HyTuv/20250629/DDRr/1415X1415/logo.png")
                        .name(history.getName())
                        .datetime(history.getDatetime())
                        .content(history.getContent())
                        .role(history.getRole())
                        .build();
            }

            chatHistoryDtos.add(chatHistoryDto);
        }


        return Result.success(chatHistoryDtos);
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
        return Result.success(historyService.getById(id));
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
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<History> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.orderByDesc(History::getId);
        return Result.success(historyService.page(new Page<>(pageNum, pageSize), queryWrapper));

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
        return Result.success(historyService.remove(new LambdaQueryWrapper<History>().eq(History::getSessionId, id)));
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
        return Result.success(historyService.removeByIds(Arrays.asList(ids)));
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

        List<History> list = historyService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("history导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), History.class).sheet("sheel1").doWrite(list);

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
        EasyExcel.read(file.getInputStream(), History.class, new PageReadListener<History>(dataList -> {
            dataList.forEach(entity -> historyService.save(entity.toBuilder().id(null).build()));
        })).sheet().doRead();
        return Result.success();
    }

}