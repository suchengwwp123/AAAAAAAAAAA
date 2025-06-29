package com.example.authority.service;


import reactor.core.publisher.Flux;

/**
 * AIservice接口
 */
public interface AiService {
    Flux<String> chat(String modelType, String prompt, boolean deepThinking);
}
