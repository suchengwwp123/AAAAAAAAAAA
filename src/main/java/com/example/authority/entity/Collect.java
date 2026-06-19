package com.example.authority.entity;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.Article ;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.User ;
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
 * @ClassName:Collect
 * @description: 收藏列表 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
@TableName("sys_collect")
@Schema(title = "Collect对象", description = "收藏列表")
    public class Collect implements Serializable{

    private static final long serialVersionUID = 1L;
        @Schema(title = "序列号")
                @TableId(value = "id", type = IdType.AUTO)
        @ExcelProperty("序列号")
private Long id;
        @Schema(title = "收藏新闻")
    @TableField("article_id")
@ExcelProperty("收藏新闻")
private Long articleId;
        @Schema(title = "收藏用户")
    @TableField("user_id")
@ExcelProperty("收藏用户")
private Long userId;
        @Schema(title = "收藏时间")
                @TableField(value = "create_time", fill = FieldFill.INSERT)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@ExcelProperty("收藏时间")
private LocalDateTime createTime;
        @TableField(exist = false)
        @ExcelIgnore
        private Article article = new Article ();
        @TableField(exist = false)
        @ExcelIgnore
        private User user = new User ();
}
