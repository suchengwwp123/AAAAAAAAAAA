package com.example.authority.service;

import reactor.core.publisher.Flux;

/**
 * deepseek接口
 */
public interface DeepSeekService {
    public String search(String prompt);
    public Flux<String> stream(String prompt);
}
