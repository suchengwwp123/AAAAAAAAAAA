package com.example.authority.entity.dto;



import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户 DTO，排除密码字段
 */
@Data
@Accessors(chain = true)
@Schema(title = "UserDto对象", description = "用户数据传输对象，排除密码字段")
public class UserDto {

    @Schema(title = "序号")
    private Long id;

    @Schema(title = "账户名")
    private String username;

    @Schema(title = "昵称")
    private String nickname;

    @Schema(title = "头像")
    private String avatar;

    @Schema(title = "手机号")
    private String phone;

    @Schema(title = "邮箱")
    private String email;

    @Schema(title = "个人简介")
    private String content;

    @Schema(title = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(title = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(title = "当前状态")
    private Long statu;

    @Schema(title = "角色编号")
    private List<Long> roleIds = new ArrayList<>();
}
