package com.example.authority.controller;

import cn.hutool.core.map.MapUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.authority.common.Result;
import com.example.authority.entity.Log;
import com.example.authority.annotations.IgoreResult;
import com.example.authority.service.LogService;
import com.example.authority.utils.RedisUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * @program: design
 * @ClassName:LogController
 * @description: LogController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = "操作日志 前端控制器")
@RestController
@RequestMapping("/log")
public class LogController {

    @Resource
    private LogService logService;
    @Resource
    private RedisUtils redisUtils;

    /**
     * 新增
     *
     * @param log
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping
    public Result save(@RequestBody Log log) {
        return Result.success(logService.save(log));
    }

    /**
     * 修改
     *
     * @param log
     * @return
     */
    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody Log log) {
        return Result.success(logService.updateById(log));
    }

    /**
     * 查询所有Log
     *
     * @return
     */
    @Operation(summary = "查询所有Log")
    @GetMapping
    public Result findAll() {
        return Result.success(logService.list());
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
        return Result.success(logService.getById(id));
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
    @IgoreResult
    public Result findPage(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<Log> lambdaQueryWrapper = new LambdaQueryWrapper();
        if (!"".equals(name)) {
            lambdaQueryWrapper.like(Log::getUsername, name);

        }
        lambdaQueryWrapper.orderByDesc(Log::getId);
        return Result.success(logService.page(new Page<>(pageNum, pageSize), lambdaQueryWrapper));
    }

    /**
     * 前台首页数据可视化展示
     * @param currentDay
     * @return
     */
    @Operation(summary = "根据日期获取访问量")
    @GetMapping("/echarts/{currentDay}")
    public Result echarts(@PathVariable Integer currentDay) {
        String cacheKey = "visit:" + currentDay;

        // 1. 先尝试从 Redis 获取缓存
        Object cacheData = redisUtils.get(cacheKey);
        if (cacheData != null && cacheData instanceof Map) {
            return Result.success(cacheData);
        }

        // 2. 如果缓存未命中，则从数据库查询
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(currentDay - 1); // 包含今天

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = today.atTime(LocalTime.MAX);

        List<Log> logs = logService.list(new LambdaQueryWrapper<Log>()
                .ge(Log::getCreateTime, startDateTime)
                .le(Log::getCreateTime, endDateTime));

        Map<String, Long> dailyCountMap = new HashMap<>();
        for (Log log : logs) {
            String date = log.getCreateTime().toLocalDate().format(formatter);
            dailyCountMap.put(date, dailyCountMap.getOrDefault(date, 0L) + 1);
        }

        List<String> dateList = new ArrayList<>();
        List<Long> seriesList = new ArrayList<>();
        for (int i = 0; i < currentDay; i++) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);
            dateList.add(dateStr);
            seriesList.add(dailyCountMap.getOrDefault(dateStr, 0L));
        }

        Collections.reverse(dateList);
        Collections.reverse(seriesList);

        Map<Object, Object> resultMap = MapUtil.builder()
                .put("dates", dateList)
                .put("counts", seriesList)
                .build();

        // 3. 放入缓存，有效期 1 小时（单位：秒）
        redisUtils.set(cacheKey, resultMap, 3600);

        return Result.success(resultMap);
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
        return Result.success(logService.removeById(id));
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
        return Result.success(logService.removeByIds(Arrays.asList(ids)));
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

        List<Log> list = logService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("log导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Log.class).sheet("sheel1").doWrite(list);

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
        EasyExcel.read(file.getInputStream(), Log.class, new PageReadListener<Log>(dataList -> {
            dataList.forEach(entity -> logService.save(entity.toBuilder().id(null).build()));
        })).sheet().doRead();
        return Result.success();
    }

}