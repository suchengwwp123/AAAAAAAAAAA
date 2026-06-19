package com.example.authority.entity;
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
 * @ClassName:Contact
 * @description: 联系记录 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
@TableName("sys_contact")
@Schema(title = "Contact对象", description = "联系记录")
    public class Contact implements Serializable{

    private static final long serialVersionUID = 1L;
        @Schema(title = "序列号")
                @TableId(value = "id", type = IdType.AUTO)
        @ExcelProperty("序列号")
private Long id;
        @Schema(title = "姓名")
    @TableField("name")
@ExcelProperty("姓名")
private String name;
        @Schema(title = "电话")
    @TableField("phone")
@ExcelProperty("电话")
private String phone;
        @Schema(title = "邮箱")
    @TableField("email")
@ExcelProperty("邮箱")
private String email;
        @Schema(title = "联系事由")
    @TableField("des")
@ExcelProperty("联系事由")
private String des;
        @Schema(title = "当前状态")
    @TableField("statu")
@ExcelProperty("当前状态")
private String statu;
}
