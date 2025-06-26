package com.example.authority.controller;

import com.example.authority.service.DeepSeekService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/ai")
public class AiController {
    @Resource
    private DeepSeekService deepSeekService;

    @PostMapping("/chat")
    public String search(@RequestBody String prompt) {
        return deepSeekService.search(prompt);
    }


    /**
     * 流式输出
     *
     * @param prompt
     * @return
     */
    @GetMapping(value = "/stream/{prompt}",produces="text/html;charset=UTF-8")
    public Flux<String> stream(@PathVariable String prompt) {
        return deepSeekService.stream(prompt);
    }

}