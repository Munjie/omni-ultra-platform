package com.munjie.omni.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

@Configuration
public class ZepConfig {

    @Value("${zep.base-url}")
    private String zepBaseUrl;

    @Value("${zep.token}")
    private String zepToken;

    /**
     * 在这里定义并向 Spring 容器注册 RestClient Bean
     */
    @Bean
    public RestClient zepClient() {
        return RestClient.builder()
                .baseUrl(zepBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + zepToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    String errorBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new RuntimeException("Zep 远端响应服务报错! 状态码: "
                            + response.getStatusCode() + ", 原因: " + errorBody);
                })
                .build();
    }
}