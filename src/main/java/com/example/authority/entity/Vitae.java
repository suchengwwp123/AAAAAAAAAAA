package com.example.authority.entity;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.User ;
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
 * @ClassName:Vitae
 * @description: 简历管理 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
@TableName("sys_vitae")
@Schema(title = "Vitae对象", description = "简历管理")
    public class Vitae implements Serializable{

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
        @Schema(title = "照片")
    @TableField("img")
@ExcelProperty("照片")
private String img;
        @Schema(title = "描述")
    @TableField("des")
@ExcelProperty("描述")
private String des;
        @Schema(title = "简历详情")
    @TableField("content")
@ExcelProperty("简历详情")
private String content;
        @Schema(title = "所属账户")
    @TableField("user_id")
@ExcelProperty("所属账户")
private Long userId;
        @Schema(title = "所在区域")
    @TableField("address")
@ExcelProperty("所在区域")
private String address;
        @Schema(title = "身份证号")
    @TableField("idnumber")
@ExcelProperty("身份证号")
private String idnumber;
        @Schema(title = "性别")
    @TableField("sex")
@ExcelProperty("性别")
private String sex;
        @Schema(title = "资质文件")
    @TableField("file")
@ExcelProperty("资质文件")
private String file;
        @Schema(title = "参考金额/天")
    @TableField("price")
@ExcelProperty("参考金额/天")
private Double price;
        @TableField(exist = false)
        @ExcelIgnore
        private User user = new User ();
}
