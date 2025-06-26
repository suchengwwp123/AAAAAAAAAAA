package com.example.authority.entity;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.Dict ;
import com.alibaba.excel.annotation.ExcelIgnore;
//import com.example.authority.entity.Grade ;
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
 * @program: authority-2026.0.2
 * @ClassName:Student
 * @description: 学生列表 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
@TableName("sys_student")
@Schema(title = "Student对象", description = "学生列表")
    public class Student implements Serializable{

    private static final long serialVersionUID = 1L;
        @Schema(title = "序列号")
                @TableId(value = "id", type = IdType.AUTO)
        @ExcelProperty("序列号")
private Long id;
        @Schema(title = "学生姓名")
    @TableField("name")
@ExcelProperty("学生姓名")
private String name;
        @Schema(title = "学生性别")
    @TableField("sex")
@ExcelProperty("学生性别")
private Long sex;
        @Schema(title = "学生班级")
    @TableField("gid")
@ExcelProperty("学生班级")
private Long gid;
        @Schema(title = "学生照片")
    @TableField("img")
@ExcelProperty("学生照片")
private String img;
        @Schema(title = "学生档案")
    @TableField("file")
@ExcelProperty("学生档案")
private String file;
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
        @TableField(exist = false)
        @ExcelIgnore
        private Dict sexDict = new Dict ();
//        @TableField(exist = false)
//        @ExcelIgnore
//        private Grade grade = new Grade ();
}
