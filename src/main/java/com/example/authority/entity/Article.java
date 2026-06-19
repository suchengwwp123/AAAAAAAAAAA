package com.example.authority.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.Category;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.User;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

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
 * @ClassName:Article
 * @description: 健康资讯 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_article")
@Schema(title = "Article对象", description = "健康资讯")
public class Article implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(title = "序列号")
    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("序列号")
    private Long id;
    @Schema(title = "标题")
    @TableField("name")
    @ExcelProperty("标题")
    private String name;
    @Schema(title = "描述")
    @TableField("des")
    @ExcelProperty("描述")
    private String des;
    @Schema(title = "封面")
    @TableField("img")
    @ExcelProperty("封面")
    private String img;
    @Schema(title = "所属分类")
    @TableField("category_id")
    @ExcelProperty("所属分类")
    private Long categoryId;
    @Schema(title = "详情")
    @TableField("content")
    @ExcelProperty("详情")
    private String content;
    @Schema(title = "创建账户")
    @TableField("user_id")
    @ExcelProperty("创建账户")
    private Long userId;
    @TableField(exist = false)
    @ExcelIgnore
    private Category category = new Category();
    @TableField(exist = false)
    @ExcelIgnore
    private User user = new User();
    @TableField(exist = false)
    @ExcelIgnore
    private Boolean isCollect=false;
}
