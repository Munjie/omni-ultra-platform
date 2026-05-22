package com.munjie.omni.infr;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ZepMemoryService {

    @Value("${zep.base-url}")
    private String zepBaseUrl;

    @Value("${zep.token}")
    private String zepToken;

    private final RestClient zepClient;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ZepMemoryService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,RestClient zepClient) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        this.zepClient = zepClient;
    }

    /**
     * 1. 异步将用户的发言追加到 Zep 的 Session 会话中
     */
    public Mono<Void> addMessageToZep(Integer userId, String content, String role) {
        Map<String, Object> message = Map.of(
                "role", role, // "user" 或 "assistant"
                "content", content
        );

        Mono<Void> voidMono = this.webClient
                .post()
                .uri(zepBaseUrl + "/sessions/" + userId + "/memory")
                .header(HttpHeaders.AUTHORIZATION, zepToken)
                .bodyValue(Map.of("messages", List.of(message)))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnError(e -> log.error("Zep 记忆同步失败", e));

        this.webClient.post()
                .uri(zepBaseUrl + "/sessions/" + userId + "/synthesize")
                .bodyValue(Map.of())
                .retrieve()
                .toBodilessEntity();
        log.info("已成功主动触发 Zep 记忆融合提取任务");
        return voidMono;
    }


    public Mono<Map> getMemoryFromZep(Integer userId) {
        Mono<Map> bearer = this.webClient.get()
                .uri(zepBaseUrl + "/sessions/" + userId + "/memory")
                .header(HttpHeaders.AUTHORIZATION, zepToken.startsWith("Bearer ") ? zepToken : "Bearer " + zepToken)
                .retrieve()
                .bodyToMono(String.class)
                .map(jsonStr -> {
                    try {
                        if (jsonStr == null || jsonStr.trim().isEmpty()) {
                            return Map.of();
                        }
                        return objectMapper.readValue(jsonStr, Map.class);
                    } catch (Exception e) {
                        throw new RuntimeException("Zep 返回文本解析成 Map 失败: " + jsonStr, e);
                    }
                })
                .doOnError(e -> log.error("从 Zep 获取记忆失败, sessionId: {}", userId, e));
        return bearer;
    }



    public Map<String, Object> getUserKnowledgeGraph(Integer userId) {
        try {
            ResponseEntity<String> response = zepClient.get()
                    .uri("/sessions/" + userId + "/memory")
                    .retrieve()
                    .toEntity(String.class);

            log.info("Zep 原始返回内容: {}", response.getBody());
        } catch (Exception e) {
            log.error("请求发生异常", e);
        }

        try {
            // 请求 Zep 的图谱增强数据接口
            Map<?, ?> graphResponse = zepClient.get()
                    .uri(zepBaseUrl +"/graph/" + userId) // 确保采用新版 API 结构
                    .retrieve()
                    .body(Map.class);
            return (Map<String, Object>) graphResponse;
        } catch (Exception e) {
            log.error("获取知识图谱失败", e);
            return Map.of();
        }
    }


}