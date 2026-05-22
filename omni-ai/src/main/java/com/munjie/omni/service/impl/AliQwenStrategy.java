package com.munjie.omni.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.munjie.omni.infr.ZepMemoryService;
import com.munjie.omni.pojo.entity.AiModelInfoEntity;
import com.munjie.omni.strategy.AiStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AliQwenStrategy implements AiStrategy {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public AliQwenStrategy(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Resource
    private ZepMemoryService zepMemoryService;

    @Override
    public String getProviderType() {
        return "ali";
    }

    @Override
    public Flux<String> handleChat(AiModelInfoEntity info, Map<String, Object> request) {
        Integer userId = 1;
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
        String userMessage = extractUserMessage(request);
//        Map<String, Object> userKnowledgeGraph = zepMemoryService.getUserKnowledgeGraph(userId);
        // 2. 串联响应式流：先向 Zep 投递用户话语 -> 再读取该用户的长期记忆摘要 -> 组装 Prompt 喂给阿里模型
        return zepMemoryService.addMessageToZep(userId, userMessage, "user")
                .then(zepMemoryService.getMemoryFromZep(userId))
                .flatMapMany(memoryMap -> {
                    String longTermSummary = "";
                    Object summaryObj = memoryMap.get("summary");
                    if (summaryObj instanceof Map) {
                        Object contentObj = ((Map<?, ?>) summaryObj).get("content");
                        longTermSummary = (contentObj != null) ? contentObj.toString() : "";
                    } else if (summaryObj instanceof String) {
                        longTermSummary = (String) summaryObj;
                    }
                    // 4. 动态把记忆作为 System Prompt 注入进阿里大模型的 request 结构中
                    injectMemoryToRequest(request, longTermSummary);
                    // StringBuilder 用来在内存中偷偷收集 AI 流式返回的每一个碎片，用于最后存入 Zep
                    StringBuilder aiReplyCollector = new StringBuilder();
                    // 5. 调用阿里大模型，获取流式 Flux
                    return executeModelCall(info, request)
                            .doOnNext(aiReplyChunk -> {
                                // 核心：在流传输的过程中，把每一个字拼起来（注意：需要根据阿里的数据格式清洗出纯文本字符串）
                                String cleanText = parseChunkText(aiReplyChunk);
                                aiReplyCollector.append(cleanText);
                            })
                            .doOnComplete(() -> {
                                if (aiReplyCollector.length() > 0) {
                                    zepMemoryService.addMessageToZep(userId, aiReplyCollector.toString(), "assistant")
                                            .subscribe();
                                }
                            });
                });
    }


    private Flux<String> executeModelCall(AiModelInfoEntity info, Map<String, Object> body) {
        return this.webClient
                .post()
                .uri(info.getBaseUrl())
                .header("Authorization", "Bearer " + info.getApiKey())
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(resBody ->
                                Mono.error(new RuntimeException("API错误: " + resBody))
                        )
                )
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(60)); // AI聊天容易超时，建议从10秒放宽至60秒
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

    /**
     * 从阿里的标准的请求体中提取出用户最后发送的那句 Prompt
     */
    private String extractUserMessage(Map<String, Object> request) {
        try {
            List<Map<String, String>> messages = (List<Map<String, String>>) request.get("messages");
            if (messages != null && !messages.isEmpty()) {
                return messages.get(messages.size() - 1).get("content");
            }
        } catch (Exception e) {
            log.warn("未能解析出标准的 messages 结构，改用默认 prompt 提取");
        }
        return String.valueOf(request.getOrDefault("prompt", ""));
    }

    /**
     * 将 Zep 记忆作为 system 设定强行压入阿里模型的请求数组最前面
     */
    private void injectMemoryToRequest(Map<String, Object> request, String longTermSummary) {
        if (longTermSummary == null || longTermSummary.trim().isEmpty()) {
            return;
        }

        List<Map<String, String>> messages = (List<Map<String, String>>) request.computeIfAbsent("messages", k -> new ArrayList<>());

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是暗黑矩阵[界云/JCloud]的智能引路人。以下是你对当前读者的记忆，请在后续对话中若隐若现地展现出你认识他，绝不能穿帮：\n" + longTermSummary);

        // 始终塞在整个聊天队列的第一句
        messages.add(0, systemMessage);
    }

    /**
     * 解析阿里流式返回的 SSE chunk，提取纯文本（防止把 JSON 格式也塞进 Zep 导致记忆被污染）
     */
    private String parseChunkText(String aiReplyChunk) {
        // 如果你前端直接接收的是原始字符串，这里需要做简单的 JSON 提取
        // 示例：阿里标准的返回通常带 data:{"output":{"choices":[{"message":{"content":"字"}}]}}
        // 如果你在别的地方已经处理过了，这里直接返回清洗后的纯汉字串即可。
        if (aiReplyChunk.contains("\"content\":\"")) {
            // 简易提取逻辑，实际开发中建议用 Jackson 或 Fastjson 转换
            int start = aiReplyChunk.indexOf("\"content\":\"") + 11;
            int end = aiReplyChunk.indexOf("\"", start);
            if (start > 10 && end > start) {
                return aiReplyChunk.substring(start, end);
            }
        }
        return ""; // 如果是心跳线或空行，返回空字符串
    }
}