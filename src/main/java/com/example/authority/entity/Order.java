package com.example.authority.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.User;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.Vitae;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import lombok.experimental.Accessors;


/**
 * @program: design
 * @ClassName:Order
 * @description: 订单列表 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_order")
@Schema(title = "Order对象", description = "订单列表")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(title = "序列号")
    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("序列号")
    private Long id;
    @Schema(title = "所属账户")
    @TableField("user_id")
    @ExcelProperty("所属账户")
    private Long userId;
    @Schema(title = "预约人员")
    @TableField("resume_id")
    @ExcelProperty("预约人员")
    private Long resumeId;
    @Schema(title = "预约地址")
    @TableField("address")
    @ExcelProperty("预约地址")
    private String address;
    @Schema(title = "预约时间")
    @TableField("time")
    @ExcelProperty("预约时间")
    private String time;
    @Schema(title = "陪诊师账号")
    @TableField("worker_id")
    @ExcelProperty("陪诊师账号")
    private Long workerId;
    @Schema(title = "当前状态")
    @TableField("statu")
    @ExcelProperty("当前状态")
    private String statu;
    @Schema(title = "其他需求")
    @TableField("des")
    @ExcelProperty("其他需求")
    private String des;
    @Schema(title = "开始时间")
    @TableField("stime")
    @ExcelProperty("开始时间")
    private String stime;
    @Schema(title = "结束时间")
    @TableField("etime")
    @ExcelProperty("结束时间")
    private String etime;
    @Schema(title = "实际费用")
    @TableField("total")
    @ExcelProperty("实际费用")
    private Double total;
    @Schema(title = "订单编号")
    @TableField("ordernum")
    @ExcelProperty("订单编号")
    private String ordernum;
    @Schema(title = "阿里流水号")
    @TableField("alinum")
    @ExcelProperty("阿里流水号")
    private String alinum;
    @Schema(title = "评分")
    @TableField("rate")
    @ExcelProperty("评分")
    private Integer rate;
    @Schema(title = "评价信息")
    @TableField("content")
    @ExcelProperty("评价信息")
    private String content;
    @Schema(title = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
    @TableField(exist = false)
    @ExcelIgnore
    private User user = new User();
    @TableField(exist = false)
    @ExcelIgnore
    private User worker = new User();
    @TableField(exist = false)
    @ExcelIgnore
    private Vitae vitae = new Vitae();
}
