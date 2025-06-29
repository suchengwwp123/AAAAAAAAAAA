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
 * @program: authority-2026.0.2
 * @ClassName:Grade
 * @description: 班级列表 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
@TableName("sys_grade")
@Schema(title = "Grade对象", description = "班级列表")
    public class Grade implements Serializable{

    private static final long serialVersionUID = 1L;
        @Schema(title = "序列号")
                @TableId(value = "id", type = IdType.AUTO)
        @ExcelProperty("序列号")
private Long id;
        @Schema(title = "班级名称")
    @TableField("grade_name")
@ExcelProperty("班级名称")
private String gradeName;
        @Schema(title = "班级编码")
    @TableField("grade_code")
@ExcelProperty("班级编码")
private String gradeCode;
        @Schema(title = "班主任")
    @TableField("grade_teacher")
@ExcelProperty("班主任")
private String gradeTeacher;
        @Schema(title = "学生人数")
    @TableField("student_count")
@ExcelProperty("学生人数")
private Integer studentCount;
        @Schema(title = "状态")
    @TableField("status")
@ExcelProperty("状态")
private Integer status;
        @Schema(title = "备注")
    @TableField("remark")
@ExcelProperty("备注")
private String remark;
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
