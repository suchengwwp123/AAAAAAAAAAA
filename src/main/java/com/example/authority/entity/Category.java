package com.example.authority.entity;
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
 * @ClassName:Category
 * @description: 文章分类 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
@TableName("sys_category")
@Schema(title = "Category对象", description = "文章分类")
    public class Category implements Serializable{

    private static final long serialVersionUID = 1L;
        @Schema(title = "序列号")
                @TableId(value = "id", type = IdType.AUTO)
        @ExcelProperty("序列号")
private Long id;
        @Schema(title = "分类名称 ")
    @TableField("name")
@ExcelProperty("分类名称 ")
private String name;
        @Schema(title = "分类图标")
    @TableField("img")
@ExcelProperty("分类图标")
private String img;
        @Schema(title = "分类描述")
    @TableField("des")
@ExcelProperty("分类描述")
private String des;
        @Schema(title = "创建时间")
                @TableField(value = "create_time", fill = FieldFill.INSERT)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@ExcelProperty("创建时间")
private LocalDateTime createTime;
        @Schema(title = "修改时间")
                @TableField(value = "update_time", fill = FieldFill.UPDATE)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@ExcelProperty("修改时间")
private LocalDateTime updateTime;
}
