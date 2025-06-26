package com.example.authority.service.impl;

import com.example.authority.service.DeepSeekService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * deepseek 服务实现类
 */

@Slf4j
@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    @Resource
    private OllamaChatModel ollamaChatModel;

    @Override
    public String search(String prompt) {
        log.info("prompt: {}",prompt);
        return ollamaChatModel.call(prompt);
    }

    @Override
    public Flux<String> stream(String prompt) {
        log.info("stream-prompt: {}",prompt);

        return ollamaChatModel.stream(prompt);
    }
}