package com.munjie.omni.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.munjie.omni.annotation.NoWrap;
import com.munjie.omni.config.UserContext;
import com.munjie.omni.constant.SystemConstant;
import com.munjie.omni.pojo.entity.AiModelInfoEntity;
import com.munjie.omni.pojo.vo.AiModelInfoVO;
import com.munjie.omni.service.AiModelInfoService;
import com.munjie.omni.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/chat")
public class AiController {

    @Resource
    private AiService aiService;

    @Resource
    private AiModelInfoService aiModelInfoService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @PostMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @NoWrap
    public Flux<String> chat(@RequestBody Map<String, Object> request) {
        Integer modelId = (Integer) request.getOrDefault("model", "DeepSeek-R1-0528-Qwen3-8B");
        return aiService.execute(modelId, request);
    }

    @GetMapping("/all-model")
    @Operation(summary ="查询所有模型")
    public List<AiModelInfoVO> allModel() {
        List<AiModelInfoEntity> list = aiModelInfoService.list();
        List<AiModelInfoVO> res = new ArrayList<>();
        if (CollUtil.isNotEmpty(list)) {
            res = list.stream().map(m -> {
                return AiModelInfoVO.builder().modelDesc(m.getModelDesc()).modelName(m.getModelName()).id(m.getId()).build();
            }).toList();
        }
        return res;
    }


    @PostMapping(value = "/save-chat")
    @Operation(summary ="保存聊天历史")
    public void saveChat(@RequestBody Map<String, Object> request) {
        String chat = request.getOrDefault("chat", "").toString();
        Integer userId = UserContext.getUserId();
        stringRedisTemplate.opsForValue().set(SystemConstant.USER_CHAT + userId, chat, Duration.ofDays(7));
    }

    @GetMapping("/get-chat-history")
    @Operation(summary ="查询所有聊天历史")
    public String getChatHistory() {
        Integer userId = UserContext.getUserId();
        String chat = stringRedisTemplate.opsForValue().get(SystemConstant.USER_CHAT + userId);
        return StrUtil.isNotBlank(chat) ? chat : StrUtil.EMPTY;
    }


}