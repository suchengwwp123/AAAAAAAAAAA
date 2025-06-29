package com.example.authority.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 关于SpringAI的配置
 */
@Configuration
public class AiConfig {

    /**
     * 使用本地大模型ollama
     * @param model
     * @return
     */
    @Bean
    public ChatClient ollamaClient(OllamaChatModel  model) {
        return ChatClient.builder(model)

                .build();
    }
    @Bean
    public ChatClient deepseekClient(DeepSeekChatModel  model) {
        return ChatClient.builder(model)

                .build();
    }
}
