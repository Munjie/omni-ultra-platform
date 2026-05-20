package com.munjie.omni.service;

import com.munjie.omni.infr.SerperSearchService;
import com.munjie.omni.pojo.entity.AiModelInfoEntity;
import com.munjie.omni.strategy.AiStrategy;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AiService {

    @Resource
    private AiModelInfoService service;


    @Resource
    private SerperSearchService serperSearchService;


    private final Map<String, AiStrategy> strategies = new ConcurrentHashMap<>();

    private final Map<Integer, ModelInstance> instances = new ConcurrentHashMap<>();

    public AiService(List<AiStrategy> strategyList) {
        strategyList.forEach(s -> strategies.put(s.getProviderType(), s));
    }


    @Data
    @AllArgsConstructor
    static class ModelInstance { AiModelInfoEntity info;AiStrategy strategy;RateLimiter rateLimiter;}

    @PostConstruct
    public void init() {
        refreshAllModels();
    }

    public synchronized void refreshAllModels() {
        log.info("开始从数据库同步模型配置...");
        List<AiModelInfoEntity> configs = service.list();
        Set<Integer> activeIds = configs.stream().map(AiModelInfoEntity::getId).collect(Collectors.toSet());
        instances.keySet().removeIf(id -> !activeIds.contains(id));
        configs.forEach(this::registerModel);
        log.info("同步完成，当前可用模型数量: {}", instances.size());
    }

    // 动态刷新/添加模型的方法
    public void registerModel(AiModelInfoEntity info) {
        AiStrategy strategy = strategies.get(info.getProviderType());
        if (strategy == null) {
            log.error("模型 {} 对应的策略 {} 未定义，跳过", info.getId(), info.getProviderType());
            return;
        }
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .limitForPeriod(info.getRateLimit())
                .timeoutDuration(Duration.ofMillis(500))
                .build();
        RateLimiter rateLimiter = RateLimiter.of(String.valueOf(info.getId()), config);
        instances.put(info.getId(), new ModelInstance(info, strategy, rateLimiter));
        log.info("模型 {} 已注册", info.getModelValue());
    }


    public Flux<String> execute(Integer modelId, Map<String, Object> request) {
        ModelInstance instance = instances.get(modelId);
        if (instance == null) {
            return Flux.just(formatErrorJson("系统错误：当前模型未注册或已下线，请选择其他模型"));
        }
        long startTime = System.currentTimeMillis();
        Boolean enableWeb = (Boolean) request.getOrDefault("search", false);
        if (!enableWeb) {
            return instance.getStrategy().handleChat(instance.getInfo(), request)
                    // 2. 绑定限流器
                    .transformDeferred(RateLimiterOperator.of(instance.getRateLimiter()))
                    .onErrorResume(throwable -> {
                        String friendlyMsg = translateException(throwable, instance.getInfo().getModelName());
                        log.error("模型调用异常 [{}]: {}", modelId, throwable.getMessage());
                        return Flux.just(formatErrorJson(friendlyMsg));
                    })
                    .doOnComplete(() -> {
                        log.info("模型 {} 调用耗时: {}ms", modelId, System.currentTimeMillis() - startTime);
                    });
        }
        List<Map<String, String>> originalMessages = (List<Map<String, String>>) request.get("messages");
        String userQuestion = originalMessages.get(originalMessages.size() - 1).get("content");
        Mono<String> stringMono = serperSearchService.searchInternet(userQuestion);
        return stringMono.flatMapMany(searchResults -> {
                    List<Map<String, Object>> newMessages = new ArrayList<>();
                    for (int i = 0; i < originalMessages.size() - 1; i++) {
                        newMessages.add(new HashMap<>(originalMessages.get(i)));
                    }
                    Map<String, Object> searchMsg = new HashMap<>();
                    searchMsg.put("role", "assistant");
                    searchMsg.put("content", """
                            我已通过实时互联网搜索获取了以下信息。请你严格遵守以下规则来回答用户问题：
                            1. 优先使用以下搜索结果中的可靠信息作为事实依据。
                            2. 如果多个结果矛盾，请选择来自权威来源（如官网、主流媒体）的内容，并注明可能存在争议。
                            3. 忽略明显广告、推广、论坛吐槽等低质量内容。
                            4. 如果搜索结果无法回答问题，请诚实说明“根据当前搜索结果无法确认”。
                            5. 回答时自然流畅，不要机械照抄搜索内容。
                            6. 如需引用来源，可在答案末尾简要提及网站名称。
                            【实时搜索结果】：
                            %s
                            【用户问题】：%s
                            """.formatted(searchResults, userQuestion));
                    System.out.println("searchResults = " + searchResults);
                    newMessages.add(searchMsg);
                    // 最后加上用户原始问题
                    Map<String, Object> userMsg = new HashMap<>();
                    userMsg.put("role", "user");
                    userMsg.put("content", userQuestion);
                    newMessages.add(userMsg);
                    request.put("messages", newMessages);
                    return instance.getStrategy().handleChat(instance.getInfo(), request);
                })
                .transformDeferred(RateLimiterOperator.of(instance.getRateLimiter()))
                .onErrorResume(throwable -> {
                    log.error("联网搜索或模型调用失败 [{}]: {}", modelId, throwable.getMessage(), throwable);
                    return Flux.just(formatErrorJson("联网搜索失败或模型响应异常，请稍后重试"));
                })
                .doOnComplete(() -> log.info("模型 {} (联网) 调用耗时: {}ms", modelId, System.currentTimeMillis() - startTime));


    }

    /**
     * 将不同的异常转换
     */
    private String translateException(Throwable t, String modelId) {
        if (t instanceof io.github.resilience4j.ratelimiter.RequestNotPermitted) {
            return "【系统提示】当前模型 [" + modelId + "] 访问太频繁，请稍后再试。";
        } else if (t instanceof java.util.concurrent.TimeoutException) {
            return "【系统提示】模型响应超时，上游供应商可能存在网络波动。";
        } else if (t instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
            var we = (org.springframework.web.reactive.function.client.WebClientResponseException) t;
            return "【供应商错误】状态码：" + we.getStatusCode() + "，信息：" + we.getResponseBodyAsString();
        }
        return "【系统错误】当前模型 [" + modelId + "] 调用失败，请选择其他模型";
    }


    private String formatErrorJson(String msg) {
        String safeMsg = msg.replace("\"", "\\\""); // 简单转义防止破坏 JSON 结构
        return "{\"choices\":[{\"delta\":{\"content\":\"❌ " + safeMsg + "\"},\"index\":0}]}";
    }
}