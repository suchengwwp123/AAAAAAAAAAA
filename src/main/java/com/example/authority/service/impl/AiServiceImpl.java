package com.example.authority.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.authority.entity.History;
import com.example.authority.service.AiService;
import com.example.authority.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ChatClient ollamaClient;
    private final ChatClient deepseekClient;
    private final HistoryService historyService;

    @Override
    public Flux<String> chat(String modelType, String prompt, boolean deepThinking) {
        // 1. 选择客户端
        ChatClient client = switch (modelType.toLowerCase()) {
            case "ollama" -> ollamaClient;
            case "deepseek" -> deepseekClient;
            default -> throw new IllegalArgumentException("未知模型类型: " + modelType);
        };

        // 2. 实时获取历史记录（不保存新消息）
        Long sessionId = StpUtil.getLoginIdAsLong();
        List<Message> messages = historyService.list(
                new LambdaQueryWrapper<History>()
                        .eq(History::getSessionId, sessionId)


                )
                        .stream()
                        .map(history -> "user".equalsIgnoreCase(history.getRole()) ?
                                new UserMessage(history.getContent()) :
                                new AssistantMessage(history.getContent()))
                        .collect(Collectors.toList());

        // 3. 构建动态系统提示
        String systemPrompt = deepThinking ?
                "【深度思考模式】请分步骤分析问题，给出结构化回答：" :
                "【直接回答模式】请简洁回复：";

        // 4. 纯流式响应（不保存消息）
        return client.prompt()
                .messages(messages)
                .system(systemPrompt + " 你是authority-ai助手")
                .user(prompt)
                .stream()
                .content();
    }
}