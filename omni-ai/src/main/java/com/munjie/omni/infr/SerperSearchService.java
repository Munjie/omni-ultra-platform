package com.munjie.omni.infr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Component
public class SerperSearchService {


    public record SerperResult(
            String title,
            String link,
            String snippet
    ) {}

    public record SerperResponse(
            List<SerperResult> organic
    ) {}

    @Value("${serper.api-key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://google.serper.dev")
            .build();

    public Mono<String> searchInternet(String query) {
        return webClient.post()
                .uri("/search")
                .header("X-API-KEY", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("q", query, "num", 10))  // 可加 gl="us", hl="en" 等参数
                .retrieve()
                .bodyToMono(SerperResponse.class)
                .map(response -> {
                    if (response.organic() == null || response.organic().isEmpty()) {
                        return "未找到可靠的搜索结果。";
                    }

                    StringBuilder sb = new StringBuilder("实时 Google 搜索结果（高质量摘要）：\n\n");
                    for (int i = 0; i < response.organic().size(); i++) {
                        SerperResult r = response.organic().get(i);
                        sb.append(String.format("%d. 【标题】%s\n   【来源】%s\n   【摘要】%s\n\n",
                                i + 1, r.title(), r.link(), r.snippet()));
                    }
                    return sb.toString();
                })
                .onErrorReturn("搜索失败，请稍后重试")
                .subscribeOn(Schedulers.boundedElastic()); // 避免阻塞
    }
}