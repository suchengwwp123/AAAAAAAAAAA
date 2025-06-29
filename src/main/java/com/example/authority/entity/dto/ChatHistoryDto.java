package com.example.authority.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistoryDto {
    private static final long serialVersionUID = 1L;
    private String avatar;           // 头像路径
    private String name;
    @JsonFormat( pattern = "yyyy-MM-dd HH:mm:ss")// 显示名称（如 authority-ai）
    private LocalDateTime datetime;  // 发送时间
    private String content;          // 消息内容
    private String role;             // 角色：user / assistant
}
