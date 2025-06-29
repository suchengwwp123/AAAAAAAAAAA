package com.example.authority.controller;

import com.example.authority.service.AiService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;


    /**
     * 流式输出
     * 自定义模型
     * 自定义深度思考
     *
     * @param modelType
     * @param prompt
     * @param deepThink
     * @return
     */
    @GetMapping(value = "/stream/{modelType}/{prompt}/{deepThink}", produces = "text/html;charset=UTF-8")
    public Flux<String> stream(

            @PathVariable String modelType,
            @PathVariable String prompt,
            @PathVariable boolean deepThink
    ) {

        return
                aiService.chat(modelType, prompt, deepThink);
    }

}