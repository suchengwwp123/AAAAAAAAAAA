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
 * @program: authority-2026.0.1
 * @ClassName:Dict
 * @description: 数据字典 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
@TableName("sys_dict")
@Schema(title = "Dict对象", description = "数据字典")
    public class Dict implements Serializable{

    private static final long serialVersionUID = 1L;
        @Schema(title = "序列号")
                @TableId(value = "id", type = IdType.AUTO)
        @ExcelProperty("序列号")
private Long id;
        @Schema(title = "字典名称")
    @TableField("name")
@ExcelProperty("字典名称")
private String name;
        @Schema(title = "唯一标识")
    @TableField("identification")
@ExcelProperty("唯一标识")
private String identification;
        @Schema(title = "当前状态")
    @TableField("statu")
@ExcelProperty("当前状态")
private Integer statu;
        @Schema(title = "字典描述")
    @TableField("des")
@ExcelProperty("字典描述")
private String des;
        @Schema(title = "字典键值")
    @TableField("assignments")
@ExcelProperty("字典键值")
private String assignments;
        @Schema(title = "排序")
    @TableField("sorts")
@ExcelProperty("排序")
private Integer sorts;
        @Schema(title = "父级目录")
    @TableField("pid")
@ExcelProperty("父级目录")
private Long pid;
        @Schema(title = "创建时间")
                @TableField(value = "create_time", fill = FieldFill.INSERT)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@ExcelProperty("创建时间")
private LocalDateTime createTime;
}
