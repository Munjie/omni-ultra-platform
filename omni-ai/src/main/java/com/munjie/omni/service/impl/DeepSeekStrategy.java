package com.munjie.omni.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.munjie.omni.pojo.entity.AiModelInfoEntity;
import com.munjie.omni.strategy.AiStrategy;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class DeepSeekStrategy implements AiStrategy {


    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public DeepSeekStrategy(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }



    @Override
    public String getProviderType() { return "deepseek"; }


    @Override
    public Flux<String> handleChat(AiModelInfoEntity info, Map<String, Object> request) {
        request.put("model", info.getModelValue());

        return this.webClient
                .post()
                .uri(info.getBaseUrl() + "/chat/completions")
                .header("Authorization", "Bearer " + info.getApiKey())
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).flatMap(body ->
                                Mono.error(new RuntimeException("API错误: " + body))
                        )
                )
                .bodyToFlux(String.class)
                .timeout(Duration.ofSeconds(10));
    }
}