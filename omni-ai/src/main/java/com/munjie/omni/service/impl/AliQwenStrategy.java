package com.munjie.omni.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munjie.omni.pojo.entity.AiModelInfoEntity;
import com.munjie.omni.strategy.AiStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class AliQwenStrategy implements AiStrategy {

    private final WebClient.Builder webClientBuilder;

    private final ObjectMapper objectMapper;

    public AliQwenStrategy(WebClient.Builder webClientBuilder,ObjectMapper objectMapper)
    {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getProviderType() {
        return "ali";
    }

    @Override
    public Flux<String> handleChat(AiModelInfoEntity info, Map<String, Object> request) {
        request.put("model", info.getModelValue());
        if ("image".equals(info.getModelValue())) {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("model",  info.getModelValue());
            Map<String, Object> inputMap = new HashMap<>();
            inputMap.put("prompt", "一间有着精致窗户的花店，漂亮的木质门，摆放着花朵");
            requestMap.put("input", inputMap);
            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("size", "1024*1024");
            paramsMap.put("n", 1);
            requestMap.put("parameters", paramsMap);
        }
        return webClientBuilder.build()
                .post()
                .uri(info.getBaseUrl())
                .header("Authorization", "Bearer " + info.getApiKey())
                .bodyValue(request)// 生成图片替换成 requestMap
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new RuntimeException("API错误: " + body))
                        )
                )
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(10));
    }



    private String convertToUniversalFormat(String rawJson) {
        // 将阿里的响应格式归一化为标准的 OpenAI 格式，方便前端统一解析
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            String content = "";

            // 路径1：尝试从标准 OpenAI 格式提取 (choices[0].message.content)
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                content = choices.get(0).path("message").path("content").asText("");
            }

            // 路径2：如果路径1失败，尝试从阿里原生格式提取 (output.choices[0].message.content)
            if (content.isEmpty()) {
                JsonNode outputChoices = root.path("output").path("choices");
                if (outputChoices.isArray() && !outputChoices.isEmpty()) {
                    content = outputChoices.get(0).path("message").path("content").asText("");
                }
            }

            // 路径3：流式增量提取 (choices[0].delta.content)
            if (content.isEmpty()) {
                content = choices.path(0).path("delta").path("content").asText("");
            }

            // 最终检查：如果还是空的，可能是一个错误响应
            if (content.isEmpty() && root.has("error")) {
                content = "API 错误提示: " + root.path("error").path("message").asText();
            }

            // 构造统一格式返回
            return String.format(
                    "{\"choices\":[{\"delta\":{\"content\":\"%s\"},\"index\":0}]}",
                    escapeJson(content)
            );
        } catch (Exception e) {
            log.error("解析阿里JSON失败: {}, 原始报文: {}", e.getMessage(), rawJson);
            return String.format("{\"choices\":[{\"delta\":{\"content\":\"[解析异常]: %s\"}}]}", e.getMessage());
        }
    }

    private String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}