package com.example.authority.entity;

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
 * @ClassName:History
 * @description: 聊天记录 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_history")
@Schema(title = "History对象", description = "聊天记录")
public class History implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(title = "序列号")
    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("序列号")
    private Long id;
    @Schema(title = "聊天账户")
    @TableField("name")
    @ExcelProperty("聊天账户")
    private String name;
    @Schema(title = "聊天时间")
    @TableField("datetime")
    @ExcelProperty("聊天时间")
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime datetime;
    @Schema(title = "聊天内容")
    @TableField("content")
    @ExcelProperty("聊天内容")
    private String content;
    @Schema(title = "角色")
    @TableField("role")
    @ExcelProperty("角色")
    private String role;
    @Schema(title = "会话Id")
    @TableField("sessionId")
    @ExcelProperty("会话Id")
    private Long sessionId;
}
