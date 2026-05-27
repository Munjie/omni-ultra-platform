package com.munjie.omni.controller;


import com.munjie.omni.annotation.NoWrap;
import com.munjie.omni.pojo.vo.AiModelInfoVO;
import com.munjie.omni.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/chat")
public class AiController {

    @Resource
    private AiService aiService;


    @PostMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @NoWrap
    public Flux<String> chat(@RequestBody Map<String, Object> request) {
        return aiService.execute(request);
    }

    @GetMapping("/all-model")
    @Operation(summary = "查询所有模型")
    public List<AiModelInfoVO> allModel() {
        return aiService.allModel();
    }


    @PostMapping(value = "/save-chat")
    @Operation(summary = "保存聊天历史")
    public void saveChat(@RequestBody Map<String, Object> request) {
        aiService.saveChat(request);
    }

    @GetMapping("/get-chat-history")
    @Operation(summary = "查询所有聊天历史")
    public String getChatHistory() {
      return aiService.getChatHistory();
    }


}