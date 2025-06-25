package com.example.authority.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class OnlineUserDTO {
    private Long loginId;
    private String id;
    private String username;
    private String loginTime;
    private String tokenValue;
    private String device;
    // 可以添加 ip、设备等信息

    // 构造方法、getter/setter 省略
}
