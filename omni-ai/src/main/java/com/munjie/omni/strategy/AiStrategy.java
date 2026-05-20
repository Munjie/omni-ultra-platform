package com.munjie.omni.strategy;


import com.munjie.omni.pojo.entity.AiModelInfoEntity;
import reactor.core.publisher.Flux;

import java.util.Map;

public interface AiStrategy {
    String getProviderType();
    Flux<String> handleChat(AiModelInfoEntity info, Map<String, Object> request);
}