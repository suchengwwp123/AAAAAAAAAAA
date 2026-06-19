package com.example.authority.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import com.example.authority.entity.dto.Reply;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * @program:
 * @ClassName:Comment
 * @description: 评论管理 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
@TableName("sys_comment")
@Schema(title = "Comment对象", description = "评论管理")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(title = "序列号")
    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("序列号")
    private Long id;

    @Schema(title = "内容")
    @TableField("content")
    @ExcelProperty("内容")
    private String content;

    @Schema(title = "父级编号")
    @TableField("parentId")
    @ExcelProperty("父级编号")
    private Long parentId;
    @Schema(title = "父级编号")
    @TableField(exist = false)
    @ExcelIgnore
    private Reply reply = new Reply();
    @Schema(title = "发布者账号")
    @TableField("uid")
    @ExcelProperty("发布者账号")
    private Long uid;
    @Schema(title = "发布者账号")
    @TableField(exist = false)
    @ExcelIgnore
    private User user = new User();
    @Schema(title = "创建时间")
    @ExcelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(title = "所属新闻")
    @TableField("aid")
    @ExcelProperty("所属新闻")
    private Long aid;


}

