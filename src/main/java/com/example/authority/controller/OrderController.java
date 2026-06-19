package com.example.authority.controller;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.authority.common.Result;
import com.example.authority.entity.*;
import com.example.authority.service.*;
import com.example.authority.utils.OrderNumberGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * @program: design
 * @ClassName:OrderController
 * @description: OrderController前端控制器
 * @author:design
 * @Version 3.0
 **/
@Tag(name = "订单列表 前端控制器")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;
    @Resource
    private UserService userService;
    @Resource
    private VitaeService vitaeService;
    @Resource
    private TicketsService ticketsService;


    /**
     * 新增
     * @param order
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping
    public Result save(@RequestBody Order order){
        // 如果订单号为空，自动生成唯一订单号
        if (StrUtil.isEmpty(order.getOrdernum())) {
            String orderNumber;
            int retryCount = 0;
            int maxRetries = 5;

            // 重试机制确保订单号唯一性
            do {
                orderNumber = OrderNumberGenerator.generateOrderNumber();
                retryCount++;

                // 检查订单号是否已存在
                LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Order::getOrdernum, orderNumber);
                long count = orderService.count(queryWrapper);

                if (count == 0) {
                    // 订单号不存在，可以使用
                    order.setOrdernum(orderNumber);
                    break;
                }

                if (retryCount >= maxRetries) {
                    return Result.error("生成订单号失败，请重试");
                }
            } while (retryCount < maxRetries);
        } else {
            // 如果手动指定了订单号，验证格式和唯一性
            if (!OrderNumberGenerator.isValidOrderNumber(order.getOrdernum())) {
                return Result.error("订单号格式不正确");
            }

            // 检查订单号是否已存在
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getOrdernum, order.getOrdernum());
            long count = orderService.count(queryWrapper);

            if (count > 0) {
                return Result.error("订单号已存在，请使用其他订单号");
            }
        }

        order.setUserId(StpUtil.getLoginIdAsLong());
        Vitae resume = vitaeService.getById(order.getResumeId());
        order.setWorkerId(resume.getUserId());
        return Result.success(orderService.save(order));
    }
    /**
     * 修改
     *
     * @param order
     * @return
     */
    @Operation(summary = "修改")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id,@RequestBody Order order) {
        // 更新订单
        boolean updateSuccess = orderService.updateById(order);
        
        // 如果订单状态为"已评价"且评分小于3分，自动创建工单
        if (updateSuccess && "已评价".equals(order.getStatu()) && order.getRate() != null && order.getRate() < 3) {
            // 创建工单
            Tickets ticket = Tickets.builder()
                    .userId(order.getUserId())          // 提交人（下单用户）
                    .vaId(order.getResumeId())          // 关联陪诊师
                    .statu("初始状态")
                    .orderId(order.getId())// 初始状态
                    .build();
            
            ticketsService.save(ticket);
            return Result.success("评价成功，由于您的评分过低，已经为您生成工单，请等候管理员处理!");
        }else{
            return Result.success(updateSuccess);
        }
        

    }

    /**
     * 查询所有Order
     * @return
     */
    @Operation(summary = "查询所有Order")
    @GetMapping
    public Result findAll(){
        return Result.success(orderService.list());
    }
    /**
     * 获取单个
     * @param id
     * @return
     */
    @Operation(summary = "获取单个")
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id){
        return Result.success(orderService.getById(id));
    }

    /**
     * 根据订单号查询订单
     * @param ordernum 订单号
     * @return
     */
    @Operation(summary = "根据订单号查询订单")
    @GetMapping("/ordernum/{ordernum}")
    public Result findByOrderNumber(@PathVariable String ordernum){
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrdernum, ordernum);
        Order order = orderService.getOne(queryWrapper);

        if (order != null) {
            // 填充关联数据
            User user = userService.getById(order.getUserId());
            if (ObjectUtil.isNotEmpty(user)){
                order.setUser(user);
            }
            Vitae resume = vitaeService.getById(order.getResumeId());
            if (ObjectUtil.isNotEmpty(resume)){
                order.setVitae(resume);
            }

        }

        return Result.success(order);
    }

    /**
     * 验证订单号是否可用
     * @param ordernum 订单号
     * @return
     */
    @Operation(summary = "验证订单号是否可用")
    @GetMapping("/validate/{ordernum}")
    public Result validateOrderNumber(@PathVariable String ordernum){
        // 验证格式
        if (!OrderNumberGenerator.isValidOrderNumber(ordernum)) {
            return Result.error("订单号格式不正确");
        }

        // 检查是否已存在
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getOrdernum, ordernum);
        long count = orderService.count(queryWrapper);

        if (count > 0) {
            return Result.error("订单号已存在");
        }

        return Result.success("订单号可用");
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
            @RequestParam(defaultValue = "") String statu,
            @RequestParam(defaultValue = "") Long resumeId,

            @RequestParam(defaultValue = "") String ordernum,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize){

        LambdaQueryWrapper<Order>queryWrapper=new LambdaQueryWrapper();
        if (userId!=null&&userId!=0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Order::getUserId,userId);
        }
        if (resumeId!=null&&resumeId!=0) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.eq(Order::getResumeId,resumeId);
        }

        if (StrUtil.isNotEmpty(ordernum)) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.like(Order::getOrdernum,ordernum);
        }
        if (StrUtil.isNotEmpty(statu)) {
            // name 不为空且不是空字符串的处理逻辑
            queryWrapper.like(Order::getStatu,statu);
        }
        if (StpUtil.hasRole("user")){
            queryWrapper.eq(Order::getUserId,StpUtil.getLoginIdAsLong());
        }
        if (StpUtil.hasRole("teacher")){
            queryWrapper.eq(Order::getWorkerId,StpUtil.getLoginIdAsLong());
        }

        queryWrapper.orderByDesc(Order::getId);
        Page<Order>page=orderService.page(new Page<>(pageNum,pageSize),queryWrapper);
        List<Order > records = page.getRecords();
        for (Order order : records) {
            User user = userService.getById(order.getUserId());
            if (ObjectUtil.isNotEmpty(user)){
                order.setUser(user);
            }
            User worker = userService.getById(order.getWorkerId());
            if (ObjectUtil.isNotEmpty(user)){
                order.setWorker(worker);
            }
            Vitae resume = vitaeService.getById(order.getResumeId());
            if (ObjectUtil.isNotEmpty(resume)){
                order.setVitae(resume);
            }

        }
        page.setRecords(records);
        return Result.success(page);

    }

    @Resource
    private ComplainService complainService;
    @Resource
    private TicketsService ticketService;
    /**
     * 单个删除
     * @param id
     * @return
     */
    @Operation(summary = "单个删除")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        complainService.remove(new LambdaQueryWrapper<Complain>().eq(Complain::getOrderId, id));
        ticketService.remove(new LambdaQueryWrapper<Tickets>().eq(Tickets::getOrderId, id));
        return Result.success(orderService.removeById(id));
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
        complainService.remove(new LambdaQueryWrapper<Complain>().eq(Complain::getOrderId, Arrays.asList(ids)));
        ticketService.remove(new LambdaQueryWrapper<Tickets>().eq(Tickets::getOrderId, Arrays.asList(ids)));
        return Result.success(orderService.removeByIds(Arrays.asList(ids)));
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

        List<Order> list = orderService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("order导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Order.class).sheet("sheel1").doWrite(list);

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
        EasyExcel.read(file.getInputStream(), Order.class, new PageReadListener<Order>(dataList -> {
            dataList.forEach(entity -> orderService.save(entity.toBuilder().id(null).build()));
        })).sheet().doRead();
        return Result.success();
    }

    /**
     * 获取营业额统计数据
     * @param days 天数（7、15、30）
     * @return 日期数组和营业额数组
     */
    @Operation(summary = "获取营业额统计数据")
    @GetMapping("/revenue/{days}")
    public Result getRevenueStatistics(@PathVariable Integer days) {
        try {
            // 计算开始日期
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);
            
            // 查询时间范围内的所有已支付或已完成的订单
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Order::getStatu, Arrays.asList("已支付", "进行中", "已完成", "已评价"));
            queryWrapper.isNotNull(Order::getTotal);
            queryWrapper.gt(Order::getTotal, 0);
            List<Order> orders = orderService.list(queryWrapper);
            
            // 创建日期到营业额的映射
            Map<String, Double> revenueMap = new LinkedHashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            
            // 初始化所有日期的营业额为0
            for (int i = 0; i < days; i++) {
                LocalDate date = startDate.plusDays(i);
                String dateStr = date.format(formatter);
                revenueMap.put(dateStr, 0.0);
            }
            
            // 统计每天的营业额
            for (Order order : orders) {
                if (order.getCreateTime() != null) {
                    LocalDate orderDate = order.getCreateTime().toLocalDate();
                    
                    // 只统计时间范围内的订单
                    if (!orderDate.isBefore(startDate) && !orderDate.isAfter(endDate)) {
                        String dateStr = orderDate.format(formatter);
                        Double currentRevenue = revenueMap.getOrDefault(dateStr, 0.0);
                        revenueMap.put(dateStr, currentRevenue + (order.getTotal() != null ? order.getTotal() : 0.0));
                    }
                }
            }
            
            // 构造返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("dates", new ArrayList<>(revenueMap.keySet()));
            result.put("revenue", new ArrayList<>(revenueMap.values()));
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取营业额统计失败：" + e.getMessage());
        }
    }

}