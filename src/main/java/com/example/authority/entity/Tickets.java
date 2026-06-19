package com.example.authority.entity;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.User ;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.Order ;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.example.authority.entity.Vitae ;
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
 * @ClassName:Tickets
 * @description: 工单记录 实体类
 * @author:dyy
 * @Version 3.0
 **/

@Data
    @Builder(toBuilder = true)
    @AllArgsConstructor
    @NoArgsConstructor
@TableName("sys_tickets")
@Schema(title = "Tickets对象", description = "工单记录")
    public class Tickets implements Serializable{

    private static final long serialVersionUID = 1L;
        @Schema(title = "序列号")
                @TableId(value = "id", type = IdType.AUTO)
        @ExcelProperty("序列号")
private Long id;
        @Schema(title = "用户信息")
    @TableField("user_id")
@ExcelProperty("用户信息")
private Long userId;
        @Schema(title = "工单信息")
    @TableField("order_id")
@ExcelProperty("工单信息")
private Long orderId;
        @Schema(title = "创建时间")
                @TableField(value = "create_time", fill = FieldFill.INSERT)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@ExcelProperty("创建时间")
private LocalDateTime createTime;
        @Schema(title = "当前状态")
    @TableField("statu")
@ExcelProperty("当前状态")
private String statu;
        @Schema(title = "处理结果")
    @TableField("ress")
@ExcelProperty("处理结果")
private String ress;
        @Schema(title = "办结时间")
                @TableField(value = "update_time", fill = FieldFill.UPDATE)
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@ExcelProperty("办结时间")
private LocalDateTime updateTime;
        @Schema(title = "陪诊师")
    @TableField("va_id")
@ExcelProperty("陪诊师")
private Long vaId;
        @TableField(exist = false)
        @ExcelIgnore
        private User user = new User ();
        @TableField(exist = false)
        @ExcelIgnore
        private Order order = new Order ();
        @TableField(exist = false)
        @ExcelIgnore
        private Vitae vitae = new Vitae ();
}
